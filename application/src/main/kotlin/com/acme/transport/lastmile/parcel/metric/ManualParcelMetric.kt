package com.acme.transport.lastmile.parcel.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual parcel metric records of lastmile's parcel work, and the rules, stores, and calculations around
// them.

/** Where a manual parcel metric is in its life cycle. */
enum class ManualParcelMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual parcel metric as the parcel team keeps it. */
data class ManualParcelMetric(
    var code: String,
    var name: String,
    var title: String,
    var shiftCode: String,
    var source: String,
    var contactEmail: String,
    var score: Double,
    var margin: Double,
    var stopCount: Int,
    var pallets: Int,
    var active: Boolean,
    var status: ManualParcelMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualParcelMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualParcelMetricStatus): Boolean {
        when (status) {
            ManualParcelMetricStatus.REQUESTED -> {
                when (next) {
                    ManualParcelMetricStatus.GRANTED, ManualParcelMetricStatus.EXPIRED -> return true
                }
                return false
            }
            ManualParcelMetricStatus.GRANTED -> return next == ManualParcelMetricStatus.REVOKED
            ManualParcelMetricStatus.REVOKED -> {
                when (next) {
                    ManualParcelMetricStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualParcelMetricStatus) {
        if (!canMoveTo(next)) {
            error("manual parcel metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round((margin - pallets.toDouble()) * 100) / 100

    fun snapshot(): ManualParcelMetric {
        return ManualParcelMetric(
            code = code,
            name = name,
            title = title,
            shiftCode = shiftCode,
            source = source,
            contactEmail = contactEmail,
            score = score,
            margin = margin,
            stopCount = stopCount,
            pallets = pallets,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual parcel metric in status requested. */
fun newManualParcelMetric(code: String, name: String): ManualParcelMetric {
    return ManualParcelMetric(
        code = code,
        name = name,
        title = "",
        shiftCode = "",
        source = "",
        contactEmail = "",
        score = 0.0,
        margin = 0.0,
        stopCount = 0,
        pallets = 0,
        active = false,
        status = ManualParcelMetricStatus.REQUESTED,
    )
}
