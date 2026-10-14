package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling tender preference is in its life cycle. */
enum class RollingTenderPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rolling tender preference as the tender team keeps it. */
data class RollingTenderPreference(
    var code: String,
    var name: String,
    var checksum: String,
    var budget: Double,
    var score: Double,
    var region: String,
    var status: RollingTenderPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTenderPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTenderPreferenceStatus): Boolean {
        when (status) {
            RollingTenderPreferenceStatus.REQUESTED -> {
                when (next) {
                    RollingTenderPreferenceStatus.GRANTED, RollingTenderPreferenceStatus.REVOKED -> return true
                }
                return false
            }
            RollingTenderPreferenceStatus.GRANTED -> {
                when (next) {
                    RollingTenderPreferenceStatus.REVOKED, RollingTenderPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
            RollingTenderPreferenceStatus.REVOKED -> return next == RollingTenderPreferenceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingTenderPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("rolling tender preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling tender preference"} $code"

    fun netExposure(): Double = round((budget - score) * 100) / 100

    fun snapshot(): RollingTenderPreference {
        return RollingTenderPreference(
            code = code,
            name = name,
            checksum = checksum,
            budget = budget,
            score = score,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling tender preference in status requested. */
fun newRollingTenderPreference(code: String, name: String): RollingTenderPreference {
    return RollingTenderPreference(
        code = code,
        name = name,
        checksum = "",
        budget = 0.0,
        score = 0.0,
        region = "",
        status = RollingTenderPreferenceStatus.REQUESTED,
    )
}
