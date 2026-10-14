package com.acme.transport.freightpay.fuelsurcharge.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel surcharge template records of freightpay's fuel surcharge work, and the rules, stores, and
// calculations around them.

/** Where a fuel surcharge template is in its life cycle. */
enum class FuelSurchargeTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a fuel surcharge template and its current status. */
data class FuelSurchargeTemplate(
    var code: String,
    var label: String,
    var capacity: Int,
    var expiresAt: Int,
    var loadKg: Double,
    var status: FuelSurchargeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeTemplateStatus): Boolean {
        when (status) {
            FuelSurchargeTemplateStatus.NEW -> {
                when (next) {
                    FuelSurchargeTemplateStatus.QUEUED -> return true
                }
                return false
            }
            FuelSurchargeTemplateStatus.QUEUED -> return next == FuelSurchargeTemplateStatus.RUNNING || next == FuelSurchargeTemplateStatus.FAILED
            FuelSurchargeTemplateStatus.RUNNING -> {
                when (next) {
                    FuelSurchargeTemplateStatus.FAILED -> return true
                }
                return false
            }
            FuelSurchargeTemplateStatus.DONE -> return next == FuelSurchargeTemplateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FuelSurchargeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge template $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"fuel surcharge template"} $code"

    fun projectedVolume(): Double = round((expiresAt.toDouble() - loadKg) * 100) / 100

    fun snapshot(): FuelSurchargeTemplate {
        return FuelSurchargeTemplate(
            code = code,
            label = label,
            capacity = capacity,
            expiresAt = expiresAt,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fuel surcharge template in status new. */
fun newFuelSurchargeTemplate(code: String, label: String): FuelSurchargeTemplate {
    return FuelSurchargeTemplate(
        code = code,
        label = label,
        capacity = 0,
        expiresAt = 0,
        loadKg = 0.0,
        status = FuelSurchargeTemplateStatus.NEW,
    )
}
