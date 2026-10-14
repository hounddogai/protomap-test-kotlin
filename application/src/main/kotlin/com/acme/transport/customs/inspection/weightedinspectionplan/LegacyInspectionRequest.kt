package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy inspection request is in its life cycle. */
enum class LegacyInspectionRequestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a legacy inspection request and its current status. */
data class LegacyInspectionRequest(
    var code: String,
    var label: String,
    var odometer: Int,
    var distanceKm: Double,
    var endsAt: Int,
    var ownerId: String,
    var status: LegacyInspectionRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyInspectionRequestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyInspectionRequestStatus): Boolean {
        when (status) {
            LegacyInspectionRequestStatus.OPEN -> {
                when (next) {
                    LegacyInspectionRequestStatus.RESOLVED -> return true
                }
                return false
            }
            LegacyInspectionRequestStatus.IN_REVIEW -> return next == LegacyInspectionRequestStatus.ARCHIVED
            LegacyInspectionRequestStatus.RESOLVED -> return next == LegacyInspectionRequestStatus.REOPENED
            LegacyInspectionRequestStatus.REOPENED -> {
                when (next) {
                    LegacyInspectionRequestStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyInspectionRequestStatus) {
        if (!canMoveTo(next)) {
            error("legacy inspection request $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"legacy inspection request"} $code"

    fun netExposure(): Double = round((endsAt.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): LegacyInspectionRequest {
        return LegacyInspectionRequest(
            code = code,
            label = label,
            odometer = odometer,
            distanceKm = distanceKm,
            endsAt = endsAt,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy inspection request in status open. */
fun newLegacyInspectionRequest(code: String, label: String): LegacyInspectionRequest {
    return LegacyInspectionRequest(
        code = code,
        label = label,
        odometer = 0,
        distanceKm = 0.0,
        endsAt = 0,
        ownerId = "",
        status = LegacyInspectionRequestStatus.OPEN,
    )
}
