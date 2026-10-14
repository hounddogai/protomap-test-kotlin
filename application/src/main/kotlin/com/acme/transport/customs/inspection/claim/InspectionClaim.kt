package com.acme.transport.customs.inspection.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection claim records of customs's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection claim is in its life cycle. */
enum class InspectionClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a inspection claim and its current status. */
data class InspectionClaim(
    var code: String,
    var title: String,
    var odometer: Int,
    var stopCount: Int,
    var ownerId: String,
    var status: InspectionClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionClaimStatus): Boolean {
        when (status) {
            InspectionClaimStatus.NEW -> return next == InspectionClaimStatus.FAILED
            InspectionClaimStatus.QUEUED -> {
                when (next) {
                    InspectionClaimStatus.RUNNING -> return true
                }
                return false
            }
            InspectionClaimStatus.RUNNING -> {
                when (next) {
                    InspectionClaimStatus.DONE -> return true
                }
                return false
            }
            InspectionClaimStatus.DONE -> {
                when (next) {
                    InspectionClaimStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionClaimStatus) {
        if (!canMoveTo(next)) {
            error("inspection claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round((odometer.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): InspectionClaim {
        return InspectionClaim(
            code = code,
            title = title,
            odometer = odometer,
            stopCount = stopCount,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a inspection claim in status new. */
fun newInspectionClaim(code: String, title: String): InspectionClaim {
    return InspectionClaim(
        code = code,
        title = title,
        odometer = 0,
        stopCount = 0,
        ownerId = "",
        status = InspectionClaimStatus.NEW,
    )
}
