package com.acme.transport.fleetops.inspection.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection window is in its life cycle. */
enum class InspectionWindowStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A inspection window as the inspection team keeps it. */
data class InspectionWindow(
    var code: String,
    var name: String,
    var amount: Double,
    var limit: Int,
    var quantity: Int,
    var status: InspectionWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionWindowStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionWindowStatus): Boolean {
        when (status) {
            InspectionWindowStatus.NEW -> {
                when (next) {
                    InspectionWindowStatus.RUNNING, InspectionWindowStatus.DONE -> return true
                }
                return false
            }
            InspectionWindowStatus.QUEUED -> return next == InspectionWindowStatus.RUNNING || next == InspectionWindowStatus.FAILED
            InspectionWindowStatus.RUNNING -> return next == InspectionWindowStatus.FAILED
            InspectionWindowStatus.DONE -> {
                when (next) {
                    InspectionWindowStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionWindowStatus) {
        if (!canMoveTo(next)) {
            error("inspection window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedValue(): Double = round((limit.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): InspectionWindow {
        return InspectionWindow(
            code = code,
            name = name,
            amount = amount,
            limit = limit,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a inspection window in status new. */
fun newInspectionWindow(code: String, name: String): InspectionWindow {
    return InspectionWindow(
        code = code,
        name = name,
        amount = 0.0,
        limit = 0,
        quantity = 0,
        status = InspectionWindowStatus.NEW,
    )
}
