package com.smartchat.gateway.provisioning;

public record CredentialRepresentation(String type, boolean temporary, String value) {
}
