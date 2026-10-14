package com.acme.transport.emissions.fuelburn.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy fuel burn group is in its life cycle. */
enum class LegacyFuelBurnGroupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One legacy fuel burn group, with what the planners need to act on it. */
data class LegacyFuelBurnGroup(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var ratio: Double,
    var threshold: Double,
    var name: String,
    var status: LegacyFuelBurnGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyFuelBurnGroupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyFuelBurnGroupStatus): Boolean {
        when (status) {
            LegacyFuelBurnGroupStatus.REQUESTED -> {
                when (next) {
                    LegacyFuelBurnGroupStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyFuelBurnGroupStatus.GRANTED -> {
                when (next) {
                    LegacyFuelBurnGroupStatus.REVOKED -> return true
                }
                return false
            }
            LegacyFuelBurnGroupStatus.REVOKED -> {
                when (next) {
                    LegacyFuelBurnGroupStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyFuelBurnGroupStatus) {
        if (!canMoveTo(next)) {
            error("legacy fuel burn group $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveExposure(): Double = round((threshold - ratio) * 100) / 100

    fun snapshot(): LegacyFuelBurnGroup {
        return LegacyFuelBurnGroup(
            code = code,
            title = title,
            expiresAt = expiresAt,
            ratio = ratio,
            threshold = threshold,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy fuel burn group in status requested. */
fun newLegacyFuelBurnGroup(code: String, title: String): LegacyFuelBurnGroup {
    return LegacyFuelBurnGroup(
        code = code,
        title = title,
        expiresAt = 0,
        ratio = 0.0,
        threshold = 0.0,
        name = "",
        status = LegacyFuelBurnGroupStatus.REQUESTED,
    )
}
