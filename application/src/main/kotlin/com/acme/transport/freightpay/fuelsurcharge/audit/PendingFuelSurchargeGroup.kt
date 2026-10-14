package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending fuel surcharge group is in its life cycle. */
enum class PendingFuelSurchargeGroupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A pending fuel surcharge group as the fuel surcharge team keeps it. */
data class PendingFuelSurchargeGroup(
    var code: String,
    var label: String,
    var ownerId: String,
    var timezone: String,
    var etaMinutes: Int,
    var floorValue: Double,
    var unit: String,
    var approved: Boolean,
    var score: Double,
    var loadKg: Double,
    var status: PendingFuelSurchargeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingFuelSurchargeGroupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingFuelSurchargeGroupStatus): Boolean {
        when (status) {
            PendingFuelSurchargeGroupStatus.NEW -> return next == PendingFuelSurchargeGroupStatus.QUEUED || next == PendingFuelSurchargeGroupStatus.DONE
            PendingFuelSurchargeGroupStatus.QUEUED -> return next == PendingFuelSurchargeGroupStatus.FAILED
            PendingFuelSurchargeGroupStatus.RUNNING -> return next == PendingFuelSurchargeGroupStatus.DONE
            PendingFuelSurchargeGroupStatus.DONE -> {
                when (next) {
                    PendingFuelSurchargeGroupStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingFuelSurchargeGroupStatus) {
        if (!canMoveTo(next)) {
            error("pending fuel surcharge group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedVolume(): Double = round((floorValue - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): PendingFuelSurchargeGroup {
        return PendingFuelSurchargeGroup(
            code = code,
            label = label,
            ownerId = ownerId,
            timezone = timezone,
            etaMinutes = etaMinutes,
            floorValue = floorValue,
            unit = unit,
            approved = approved,
            score = score,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending fuel surcharge group in status new. */
fun newPendingFuelSurchargeGroup(code: String, label: String): PendingFuelSurchargeGroup {
    return PendingFuelSurchargeGroup(
        code = code,
        label = label,
        ownerId = "",
        timezone = "",
        etaMinutes = 0,
        floorValue = 0.0,
        unit = "",
        approved = false,
        score = 0.0,
        loadKg = 0.0,
        status = PendingFuelSurchargeGroupStatus.NEW,
    )
}
