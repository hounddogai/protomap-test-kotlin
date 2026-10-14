package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual proof of delivery plan is in its life cycle. */
enum class ManualProofOfDeliveryPlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A manual proof of delivery plan as the proof of delivery team keeps it. */
data class ManualProofOfDeliveryPlan(
    var code: String,
    var title: String,
    var driverId: String,
    var floorValue: Double,
    var revision: Int,
    var expiresAt: Int,
    var status: ManualProofOfDeliveryPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualProofOfDeliveryPlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualProofOfDeliveryPlanStatus): Boolean {
        when (status) {
            ManualProofOfDeliveryPlanStatus.NEW -> {
                when (next) {
                    ManualProofOfDeliveryPlanStatus.RUNNING -> return true
                }
                return false
            }
            ManualProofOfDeliveryPlanStatus.QUEUED -> return next == ManualProofOfDeliveryPlanStatus.RUNNING || next == ManualProofOfDeliveryPlanStatus.DONE
            ManualProofOfDeliveryPlanStatus.RUNNING -> return next == ManualProofOfDeliveryPlanStatus.FAILED
            ManualProofOfDeliveryPlanStatus.DONE -> {
                when (next) {
                    ManualProofOfDeliveryPlanStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualProofOfDeliveryPlanStatus) {
        if (!canMoveTo(next)) {
            error("manual proof of delivery plan $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"manual proof of delivery plan"} $code"

    fun adjustedExposure(): Double = round((expiresAt.toDouble() - floorValue) * 100) / 100

    fun snapshot(): ManualProofOfDeliveryPlan {
        return ManualProofOfDeliveryPlan(
            code = code,
            title = title,
            driverId = driverId,
            floorValue = floorValue,
            revision = revision,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual proof of delivery plan in status new. */
fun newManualProofOfDeliveryPlan(code: String, title: String): ManualProofOfDeliveryPlan {
    return ManualProofOfDeliveryPlan(
        code = code,
        title = title,
        driverId = "",
        floorValue = 0.0,
        revision = 0,
        expiresAt = 0,
        status = ManualProofOfDeliveryPlanStatus.NEW,
    )
}
