package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional violation digest is in its life cycle. */
enum class RegionalViolationDigestStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One regional violation digest, with what the planners need to act on it. */
data class RegionalViolationDigest(
    var code: String,
    var name: String,
    var segment: String,
    var retries: Int,
    var pinned: Boolean,
    var visible: Boolean,
    var budget: Double,
    var status: RegionalViolationDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalViolationDigestStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalViolationDigestStatus): Boolean {
        when (status) {
            RegionalViolationDigestStatus.CREATED -> return next == RegionalViolationDigestStatus.VALIDATED || next == RegionalViolationDigestStatus.PUBLISHED
            RegionalViolationDigestStatus.VALIDATED -> return next == RegionalViolationDigestStatus.PUBLISHED || next == RegionalViolationDigestStatus.RETIRED
            RegionalViolationDigestStatus.PUBLISHED -> {
                when (next) {
                    RegionalViolationDigestStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalViolationDigestStatus) {
        if (!canMoveTo(next)) {
            error("regional violation digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round((retries.toDouble() - budget) * 100) / 100

    fun snapshot(): RegionalViolationDigest {
        return RegionalViolationDigest(
            code = code,
            name = name,
            segment = segment,
            retries = retries,
            pinned = pinned,
            visible = visible,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a regional violation digest in status created. */
fun newRegionalViolationDigest(code: String, name: String): RegionalViolationDigest {
    return RegionalViolationDigest(
        code = code,
        name = name,
        segment = "",
        retries = 0,
        pinned = false,
        visible = false,
        budget = 0.0,
        status = RegionalViolationDigestStatus.CREATED,
    )
}
