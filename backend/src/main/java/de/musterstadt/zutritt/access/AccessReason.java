package de.musterstadt.zutritt.access;

/**
 * Result reason of an access check. Stored as text in access_attempt.reason.
 */
public enum AccessReason {
    GRANTED,
    UNDERAGE,
    BLOCKED
}
