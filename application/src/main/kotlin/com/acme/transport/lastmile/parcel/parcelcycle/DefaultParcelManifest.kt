package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default parcel manifest is in its life cycle. */
enum class DefaultParcelManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One default parcel manifest, with what the planners need to act on it. */
data class DefaultParcelManifest(
    var code: String,
    var label: String,
    var carrier: String,
    var approved: Boolean,
    var summary: String,
    var revision: Int,
    var cost: Double,
    var odometer: Int,
    var score: Double,
    var margin: Double,
    var status: DefaultParcelManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultParcelManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultParcelManifestStatus): Boolean {
        when (status) {
            DefaultParcelManifestStatus.OPEN -> {
                when (next) {
                    DefaultParcelManifestStatus.REOPENED -> return true
                }
                return false
            }
            DefaultParcelManifestStatus.IN_REVIEW -> {
                when (next) {
                    DefaultParcelManifestStatus.REOPENED -> return true
                }
                return false
            }
            DefaultParcelManifestStatus.RESOLVED -> {
                when (next) {
                    DefaultParcelManifestStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultParcelManifestStatus.REOPENED -> return next == DefaultParcelManifestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DefaultParcelManifestStatus) {
        if (!canMoveTo(next)) {
            error("default parcel manifest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"default parcel manifest"} $code"

    fun weightedExposure(): Double = round((revision.toDouble() - margin) * 100) / 100

    fun snapshot(): DefaultParcelManifest {
        return DefaultParcelManifest(
            code = code,
            label = label,
            carrier = carrier,
            approved = approved,
            summary = summary,
            revision = revision,
            cost = cost,
            odometer = odometer,
            score = score,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default parcel manifest in status open. */
fun newDefaultParcelManifest(code: String, label: String): DefaultParcelManifest {
    return DefaultParcelManifest(
        code = code,
        label = label,
        carrier = "",
        approved = false,
        summary = "",
        revision = 0,
        cost = 0.0,
        odometer = 0,
        score = 0.0,
        margin = 0.0,
        status = DefaultParcelManifestStatus.OPEN,
    )
}
