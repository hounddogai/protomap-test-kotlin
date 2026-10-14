package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted lane plan manifest is in its life cycle. */
enum class WeightedLanePlanManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One weighted lane plan manifest, with what the planners need to act on it. */
data class WeightedLanePlanManifest(
    var code: String,
    var title: String,
    var limit: Int,
    var visible: Boolean,
    var startsAt: Int,
    var unit: String,
    var odometer: Int,
    var currency: String,
    var externalRef: String,
    var status: WeightedLanePlanManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedLanePlanManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedLanePlanManifestStatus): Boolean {
        when (status) {
            WeightedLanePlanManifestStatus.DRAFT -> return next == WeightedLanePlanManifestStatus.PENDING || next == WeightedLanePlanManifestStatus.SUSPENDED
            WeightedLanePlanManifestStatus.PENDING -> {
                when (next) {
                    WeightedLanePlanManifestStatus.ACTIVE -> return true
                }
                return false
            }
            WeightedLanePlanManifestStatus.ACTIVE -> return next == WeightedLanePlanManifestStatus.SUSPENDED || next == WeightedLanePlanManifestStatus.CLOSED
            WeightedLanePlanManifestStatus.SUSPENDED -> {
                when (next) {
                    WeightedLanePlanManifestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedLanePlanManifestStatus) {
        if (!canMoveTo(next)) {
            error("weighted lane plan manifest $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"weighted lane plan manifest"} $code"

    fun effectiveExposure(): Double = round(odometer.toDouble() * limit.toDouble() * 100) / 100

    fun snapshot(): WeightedLanePlanManifest {
        return WeightedLanePlanManifest(
            code = code,
            title = title,
            limit = limit,
            visible = visible,
            startsAt = startsAt,
            unit = unit,
            odometer = odometer,
            currency = currency,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a weighted lane plan manifest in status draft. */
fun newWeightedLanePlanManifest(code: String, title: String): WeightedLanePlanManifest {
    return WeightedLanePlanManifest(
        code = code,
        title = title,
        limit = 0,
        visible = false,
        startsAt = 0,
        unit = "",
        odometer = 0,
        currency = "",
        externalRef = "",
        status = WeightedLanePlanManifestStatus.DRAFT,
    )
}
