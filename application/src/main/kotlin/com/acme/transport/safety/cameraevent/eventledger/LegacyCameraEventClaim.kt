package com.acme.transport.safety.cameraevent.eventledger

import com.acme.transport.kit.text.normalize

/** Where a legacy camera event claim is in its life cycle. */
enum class LegacyCameraEventClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a legacy camera event claim and its current status. */
data class LegacyCameraEventClaim(
    var code: String,
    var name: String,
    var attributes: MutableMap<String, String>,
    var score: Double,
    var phone: String,
    var segment: String,
    var checksum: String,
    var contactEmail: String,
    var status: LegacyCameraEventClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCameraEventClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCameraEventClaimStatus): Boolean {
        when (status) {
            LegacyCameraEventClaimStatus.OPEN -> return next == LegacyCameraEventClaimStatus.RESOLVED || next == LegacyCameraEventClaimStatus.REOPENED
            LegacyCameraEventClaimStatus.IN_REVIEW -> {
                when (next) {
                    LegacyCameraEventClaimStatus.RESOLVED -> return true
                }
                return false
            }
            LegacyCameraEventClaimStatus.RESOLVED -> {
                when (next) {
                    LegacyCameraEventClaimStatus.REOPENED -> return true
                }
                return false
            }
            LegacyCameraEventClaimStatus.REOPENED -> return next == LegacyCameraEventClaimStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LegacyCameraEventClaimStatus) {
        if (!canMoveTo(next)) {
            error("legacy camera event claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = score

    fun snapshot(): LegacyCameraEventClaim {
        return LegacyCameraEventClaim(
            code = code,
            name = name,
            attributes = attributes,
            score = score,
            phone = phone,
            segment = segment,
            checksum = checksum,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a legacy camera event claim in status open. */
fun newLegacyCameraEventClaim(code: String, name: String): LegacyCameraEventClaim {
    return LegacyCameraEventClaim(
        code = code,
        name = name,
        attributes = mutableMapOf<String, String>(),
        score = 0.0,
        phone = "",
        segment = "",
        checksum = "",
        contactEmail = "",
        status = LegacyCameraEventClaimStatus.OPEN,
    )
}
