package com.acme.transport.freightpay.fuelsurcharge.surchargeentry

import com.acme.transport.kit.text.normalize

/** Where a default fuel surcharge entry is in its life cycle. */
enum class DefaultFuelSurchargeEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One default fuel surcharge entry, with what the planners need to act on it. */
data class DefaultFuelSurchargeEntry(
    var code: String,
    var label: String,
    var carrier: String,
    var trailerId: String,
    var routeCode: String,
    var score: Double,
    var status: DefaultFuelSurchargeEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultFuelSurchargeEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultFuelSurchargeEntryStatus): Boolean {
        when (status) {
            DefaultFuelSurchargeEntryStatus.NEW -> return next == DefaultFuelSurchargeEntryStatus.RUNNING
            DefaultFuelSurchargeEntryStatus.QUEUED -> {
                when (next) {
                    DefaultFuelSurchargeEntryStatus.DONE -> return true
                }
                return false
            }
            DefaultFuelSurchargeEntryStatus.RUNNING -> {
                when (next) {
                    DefaultFuelSurchargeEntryStatus.DONE -> return true
                }
                return false
            }
            DefaultFuelSurchargeEntryStatus.DONE -> {
                when (next) {
                    DefaultFuelSurchargeEntryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultFuelSurchargeEntryStatus) {
        if (!canMoveTo(next)) {
            error("default fuel surcharge entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = score

    fun snapshot(): DefaultFuelSurchargeEntry {
        return DefaultFuelSurchargeEntry(
            code = code,
            label = label,
            carrier = carrier,
            trailerId = trailerId,
            routeCode = routeCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default fuel surcharge entry in status new. */
fun newDefaultFuelSurchargeEntry(code: String, label: String): DefaultFuelSurchargeEntry {
    return DefaultFuelSurchargeEntry(
        code = code,
        label = label,
        carrier = "",
        trailerId = "",
        routeCode = "",
        score = 0.0,
        status = DefaultFuelSurchargeEntryStatus.NEW,
    )
}
