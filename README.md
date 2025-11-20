# app-gateway
gateway to manage traffic incoming from client

## User provisioning flow

Unauthenticated sign-ups are handled by the `/user/create` endpoint on the gateway:

1. `UserProvisioningController` receives the payload and forwards it to the user-service via `UserServiceClient` using the configured base URL and create path.
2. After the user-service responds successfully, the controller triggers `KeycloakUserClient` to create the user in Keycloak using the same payload fields (`userId`, `username`, `displayName`, `bio`, `avatarUrl`).
3. `KeycloakUserClient` first obtains an admin access token, then posts a `KeycloakUserRepresentation` containing the user attributes and a deterministic SHA-256/Base64 password derived from `userId` so Keycloak accepts the account.
4. If both downstream calls succeed, the gateway returns `201 Created` to the caller.

These steps keep user metadata consistent between the user-service and Keycloak while providing the required credential payload for Keycloak provisioning.
