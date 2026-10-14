package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback drop digest is in its life cycle. */
enum class FallbackDropDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fallback drop digest. Its code is unique within a tenant. */
data class FallbackDropDigest(
    var code: String,
    var label: String,
    var revision: Int,
    var batchSize: Int,
    var floorValue: Double,
    var weight: Double,
    var minimum: Double,
    var externalRef: String,
    var odometer: Int,
    var windowDays: Int,
    var status: FallbackDropDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDropDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDropDigestStatus): Boolean {
        when (status) {
            FallbackDropDigestStatus.PLANNED -> {
                when (next) {
                    FallbackDropDigestStatus.SCHEDULED -> return true
                }
                return false
            }
            FallbackDropDigestStatus.SCHEDULED -> {
                when (next) {
                    FallbackDropDigestStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackDropDigestStatus.IN_PROGRESS -> return next == FallbackDropDigestStatus.COMPLETED || next == FallbackDropDigestStatus.CANCELLED
            FallbackDropDigestStatus.COMPLETED -> {
                when (next) {
                    FallbackDropDigestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDropDigestStatus) {
        if (!canMoveTo(next)) {
            error("fallback drop digest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveExposure(): Double = round((batchSize.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): FallbackDropDigest {
        return FallbackDropDigest(
            code = code,
            label = label,
            revision = revision,
            batchSize = batchSize,
            floorValue = floorValue,
            weight = weight,
            minimum = minimum,
            externalRef = externalRef,
            odometer = odometer,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback drop digest in status planned. */
fun newFallbackDropDigest(code: String, label: String): FallbackDropDigest {
    return FallbackDropDigest(
        code = code,
        label = label,
        revision = 0,
        batchSize = 0,
        floorValue = 0.0,
        weight = 0.0,
        minimum = 0.0,
        externalRef = "",
        odometer = 0,
        windowDays = 0,
        status = FallbackDropDigestStatus.PLANNED,
    )
}
