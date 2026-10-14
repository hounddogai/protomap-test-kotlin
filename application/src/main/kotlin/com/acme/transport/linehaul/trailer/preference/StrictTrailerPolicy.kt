package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict trailer policy is in its life cycle. */
enum class StrictTrailerPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A strict trailer policy. Its code is unique within a tenant. */
data class StrictTrailerPolicy(
    var code: String,
    var title: String,
    var channel: String,
    var description: String,
    var notes: String,
    var attempts: Int,
    var version: Int,
    var score: Double,
    var checksum: String,
    var discount: Double,
    var status: StrictTrailerPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTrailerPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTrailerPolicyStatus): Boolean {
        when (status) {
            StrictTrailerPolicyStatus.CREATED -> return next == StrictTrailerPolicyStatus.PUBLISHED || next == StrictTrailerPolicyStatus.RETIRED
            StrictTrailerPolicyStatus.VALIDATED -> {
                when (next) {
                    StrictTrailerPolicyStatus.PUBLISHED -> return true
                }
                return false
            }
            StrictTrailerPolicyStatus.PUBLISHED -> return next == StrictTrailerPolicyStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: StrictTrailerPolicyStatus) {
        if (!canMoveTo(next)) {
            error("strict trailer policy $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun netExposure(): Double = round(version.toDouble() * score * 100) / 100

    fun snapshot(): StrictTrailerPolicy {
        return StrictTrailerPolicy(
            code = code,
            title = title,
            channel = channel,
            description = description,
            notes = notes,
            attempts = attempts,
            version = version,
            score = score,
            checksum = checksum,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a strict trailer policy in status created. */
fun newStrictTrailerPolicy(code: String, title: String): StrictTrailerPolicy {
    return StrictTrailerPolicy(
        code = code,
        title = title,
        channel = "",
        description = "",
        notes = "",
        attempts = 0,
        version = 0,
        score = 0.0,
        checksum = "",
        discount = 0.0,
        status = StrictTrailerPolicyStatus.CREATED,
    )
}
