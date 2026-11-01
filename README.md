# Tazkara - Event Ticketing Platform

Tazkara is a Spring Boot microservices backend for managing event ticket sales. It helps organizers sell tickets, manage reservations, prevent overselling, handle cancellations and refunds, and track sales.

## Architecture

The system consists of an API Gateway, three microservices, PostgreSQL, and RabbitMQ.

```text
[ Client / Postman ]
        |
        | Port 8080
        v
   [ API Gateway ]
        |
        |-- /api/auth/**, /api/users/** ------> Identity Service (8081)
        |
        |-- /api/events/**, /api/organizer/** -> Event Service (8082)
        |
        |-- /api/attendee/**, /bookings ------> Booking Service (8083)
                                                   |
                                                   v
                                               RabbitMQ
                                                   |
                                               PostgreSQL
                                      (identity_db, event_db, booking_db)
```

Each service manages its own domain and database schema. Services communicate through REST APIs or RabbitMQ instead of accessing each other's tables.

## Running the Project

The application runs inside a single Docker container managed by supervisord. You don't need to install Java or Maven locally.

1. Make sure Docker and Docker Compose are installed.
2. Set the following environment variables in your environment or in a `.env` file in the project root:

```env
DB_PASSWORD=postgres
JWT_SECRET=your_super_secret_jwt_key_at_least_32_characters_long
INTERNAL_API_KEY=your_internal_service_api_key_12345
```

3. Build and start the application:

```bash
docker compose up --build
```

The first startup may take around 5–10 minutes while PostgreSQL, RabbitMQ, and the Spring Boot services initialize.

## Ports and Swagger

- API Gateway: http://localhost:8080
- Identity Swagger: http://localhost:8081/swagger-ui.html
- Event Swagger: http://localhost:8082/swagger-ui.html
- Booking Swagger: http://localhost:8083/swagger-ui.html
- RabbitMQ Dashboard: http://localhost:15672

RabbitMQ credentials: `guest / guest`

The API Gateway is the main entry point for API requests.

## Demo Accounts

The `DemoData` runners seed the following data when the database is empty.

**Organizer**
- Email: `organizer@tazkara.com`
- Password: `Password123`

**Attendee**
- Email: `attendee@tazkara.com`
- Password: `Password123`

Demo event:

- Name: Amman Dance Night
- Location: Roman Theatre, Amman
- Starts in 60 days
- 200 Standard tickets at 25.00 JOD
- 50 VIP tickets at 80.00 JOD

## Postman Collection

The Postman collection is available in the project root:

`Tazkara.postman_collection.json`

Import it into Postman and set the base URL to:

`http://localhost:8080`

## Main Flow

The following steps demonstrate the main attendee and organizer operations.

1. **Login**

   Send `POST /api/auth/login` with the attendee credentials:

   ```json
   {
     "email": "attendee@tazkara.com",
     "password": "Password123"
   }
   ```

   Copy the returned access token.

2. **Browse Events**

   Send `GET /api/events`.

   This endpoint is public and does not require authentication.

3. **Create a Booking**

   Send `POST /api/attendee/bookings`:

   ```json
   {
     "eventId": 1,
     "lines": [
       {
         "ticketTypeId": 1,
         "quantity": 2
       }
     ]
   }
   ```

   The booking is created with `PENDING_PAYMENT` status and a 10-minute reservation hold.

4. **Pay for the Booking**

   Send `POST /api/attendee/bookings/{reference}/pay`:

   ```json
   {
     "paymentMethod": "CARD"
   }
   ```

   The booking status changes to `CONFIRMED`.

5. **Download Tickets**

   Send `GET /api/attendee/bookings/{reference}/tickets` to download the PDF tickets.

6. **Cancel a Booking**

   Send `POST /api/attendee/bookings/{reference}/cancel`.

   The refund is calculated according to the time remaining before the event.

7. **View Organizer Sales**

   Login using the organizer account.

  - `GET /api/organizer/events/{id}/sales` returns sales information.
  - `GET /api/organizer/events/{id}/bookings/export` exports confirmed bookings as CSV.

8. **Cancel an Event**

   Send `POST /api/organizer/events/{id}/cancel`.

   The Event Service publishes a cancellation event through RabbitMQ. The Booking Service processes it and handles the affected bookings and refunds.

## Design Decisions

### Service Communication

The application uses REST for operations that require an immediate response and RabbitMQ for asynchronous communication.

REST is used for:

- Validating event status, start time, and ticket prices.
- Reserving and releasing tickets.
- Checking event ownership before organizer operations.

RabbitMQ is used for event cancellation.

When an event is cancelled, the Event Service publishes a message after the database transaction commits. The Booking Service consumes the message and processes the affected bookings.

The queue is durable, so messages remain available if the Booking Service is temporarily offline.

### Preventing Overselling

The Event Service owns ticket inventory.

Available tickets are calculated using:

```text
available = quantity - reservedCount
```

The Booking Service requests ticket reservations from the Event Service. Ticket prices are also retrieved from the Event Service instead of being provided by the client.

To prevent overselling, the Event Service uses PostgreSQL pessimistic write locks:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

This ensures that concurrent reservation requests are processed safely. If two attendees try to reserve the last available ticket, only one succeeds.

### Internal API Security

Internal endpoints under `/api/internal/**` are protected using an API key.

The key is sent through the `X-Internal-Key` header and validated by `InternalKeyFilter`.

The filter uses constant-time comparison with `MessageDigest.isEqual()` and grants the `ROLE_INTERNAL` authority.

The API Gateway does not expose internal endpoints to external clients.

### Handling Failures

The application handles failures between services using timeouts and compensating actions.

- If the Event Service is unavailable during booking, the request returns `503 EVENT_SERVICE_UNAVAILABLE` and no booking is created.
- If tickets are reserved but saving the booking fails, the Booking Service sends a release request to the Event Service.
- If releasing tickets fails during booking expiry, the `ticketsReleased` flag remains false. The expiry scheduler retries the operation later.

### Booking Expiry and Concurrency

`BookingExpiryJob` runs periodically using Spring's `@Scheduled` annotation on a dedicated scheduling thread.

To handle situations where an attendee pays while the expiry job is processing the same booking, the Booking entity uses JPA optimistic locking through `@Version`.

Only one transaction can successfully update the booking version. The other transaction fails with an optimistic locking exception.

## Design Patterns

- **Builder:** Used to construct bookings with multiple lines, references, and timestamps.
- **Factory:** Used for creating entities in valid initial states, such as registering users and creating draft events. `RefundPolicyFactory` also selects the appropriate refund policy.
- **Strategy:** Different refund policies (`FullRefundPolicy`, `HalfRefundPolicy`, and `NoRefundPolicy`) implement the `RefundPolicy` interface.
- **Singleton:** Spring-managed beans, such as services and components, use the singleton scope by default.
- **Observer / Pub-Sub:** Booking status changes publish events handled by independent listeners. RabbitMQ is also used for communication between services.
- **MVC:** The application separates controllers, services, repositories, and domain entities, with dedicated request and response DTOs.

## Assumptions

- Currency: All prices are in JOD and stored using `BigDecimal` with two decimal places.
- Time: Timestamps are stored in UTC using `java.time.Instant`.
- Ticket limit: An attendee can have at most 6 active tickets per event across `PENDING_PAYMENT` and `CONFIRMED` bookings.
- Refund policy:
  - 7 days or more before the event: 100% refund.
  - Between 48 hours and 7 days: 50% refund.
  - Less than 48 hours: Cancellation is not allowed.
  - Pending payment bookings can be cancelled at any time without a refund.
- Free bookings: Bookings with a total amount of 0.00 JOD are confirmed immediately.
- Passwords: BCrypt with strength 12.
- Security: CSRF is disabled because authentication uses stateless JWTs in the Authorization header rather than browser cookies.
- Database: Hibernate `ddl-auto=update` is used to simplify the internship demonstration.

## Known Limitations

- Single container: All services, PostgreSQL, and RabbitMQ run inside one container under supervisord to satisfy the assignment requirements. In production, each service would run in its own container.
- Access token revocation: Access tokens remain valid until expiration (30 minutes), even after logout. Refresh tokens are revoked in the database when the user logs out.