package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered rate card manifest is in its life cycle. */
enum class TieredRateCardManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One tiered rate card manifest, with what the planners need to act on it. */
data class TieredRateCardManifest(
    var code: String,
    var title: String,
    var odometer: Int,
    var priority: Int,
    var minimum: Double,
    var status: TieredRateCardManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRateCardManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRateCardManifestStatus): Boolean {
        when (status) {
            TieredRateCardManifestStatus.PLANNED -> {
                when (next) {
                    TieredRateCardManifestStatus.CANCELLED -> return true
                }
                return false
            }
            TieredRateCardManifestStatus.SCHEDULED -> {
                when (next) {
                    TieredRateCardManifestStatus.COMPLETED -> return true
                }
                return false
            }
            TieredRateCardManifestStatus.IN_PROGRESS -> return next == TieredRateCardManifestStatus.COMPLETED || next == TieredRateCardManifestStatus.CANCELLED
            TieredRateCardManifestStatus.COMPLETED -> {
                when (next) {
                    TieredRateCardManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredRateCardManifestStatus) {
        if (!canMoveTo(next)) {
            error("tiered rate card manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered rate card manifest"} $code"

    fun effectiveValue(): Double = round((priority.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): TieredRateCardManifest {
        return TieredRateCardManifest(
            code = code,
            title = title,
            odometer = odometer,
            priority = priority,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered rate card manifest in status planned. */
fun newTieredRateCardManifest(code: String, title: String): TieredRateCardManifest {
    return TieredRateCardManifest(
        code = code,
        title = title,
        odometer = 0,
        priority = 0,
        minimum = 0.0,
        status = TieredRateCardManifestStatus.PLANNED,
    )
}
