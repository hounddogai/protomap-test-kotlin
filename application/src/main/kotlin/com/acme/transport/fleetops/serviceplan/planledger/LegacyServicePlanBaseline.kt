package com.acme.transport.fleetops.serviceplan.planledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy service plan baseline is in its life cycle. */
enum class LegacyServicePlanBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a legacy service plan baseline and its current status. */
data class LegacyServicePlanBaseline(
    var code: String,
    var name: String,
    var endsAt: Int,
    var vehicleId: String,
    var ownerId: String,
    var amount: Double,
    var retries: Int,
    var threshold: Double,
    var externalRef: String,
    var description: String,
    var quantity: Int,
    var status: LegacyServicePlanBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyServicePlanBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyServicePlanBaselineStatus): Boolean {
        when (status) {
            LegacyServicePlanBaselineStatus.IDLE -> {
                when (next) {
                    LegacyServicePlanBaselineStatus.WARMING, LegacyServicePlanBaselineStatus.DRAINING -> return true
                }
                return false
            }
            LegacyServicePlanBaselineStatus.WARMING -> return next == LegacyServicePlanBaselineStatus.STOPPED
            LegacyServicePlanBaselineStatus.READY -> return next == LegacyServicePlanBaselineStatus.DRAINING || next == LegacyServicePlanBaselineStatus.STOPPED
            LegacyServicePlanBaselineStatus.DRAINING -> {
                when (next) {
                    LegacyServicePlanBaselineStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyServicePlanBaselineStatus) {
        if (!canMoveTo(next)) {
            error("legacy service plan baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun netVolume(): Double = round((retries.toDouble() - amount) * 100) / 100

    fun snapshot(): LegacyServicePlanBaseline {
        return LegacyServicePlanBaseline(
            code = code,
            name = name,
            endsAt = endsAt,
            vehicleId = vehicleId,
            ownerId = ownerId,
            amount = amount,
            retries = retries,
            threshold = threshold,
            externalRef = externalRef,
            description = description,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy service plan baseline in status idle. */
fun newLegacyServicePlanBaseline(code: String, name: String): LegacyServicePlanBaseline {
    return LegacyServicePlanBaseline(
        code = code,
        name = name,
        endsAt = 0,
        vehicleId = "",
        ownerId = "",
        amount = 0.0,
        retries = 0,
        threshold = 0.0,
        externalRef = "",
        description = "",
        quantity = 0,
        status = LegacyServicePlanBaselineStatus.IDLE,
    )
}
