package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection exception is in its life cycle. */
enum class InspectionExceptionStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One inspection exception, with what the planners need to act on it. */
data class InspectionException(
    var code: String,
    var name: String,
    var budget: Double,
    var externalRef: String,
    var floorValue: Double,
    var active: Boolean,
    var label: String,
    var summary: String,
    var stopCount: Int,
    var fuelLiters: Double,
    var locale: String,
    var status: InspectionExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionExceptionStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionExceptionStatus): Boolean {
        when (status) {
            InspectionExceptionStatus.NEW -> return next == InspectionExceptionStatus.RUNNING || next == InspectionExceptionStatus.DONE
            InspectionExceptionStatus.QUEUED -> return next == InspectionExceptionStatus.RUNNING || next == InspectionExceptionStatus.FAILED
            InspectionExceptionStatus.RUNNING -> {
                when (next) {
                    InspectionExceptionStatus.DONE, InspectionExceptionStatus.FAILED -> return true
                }
                return false
            }
            InspectionExceptionStatus.DONE -> {
                when (next) {
                    InspectionExceptionStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionExceptionStatus) {
        if (!canMoveTo(next)) {
            error("inspection exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $name"

    fun effectiveVolume(): Double = round((budget - stopCount.toDouble()) * 100) / 100

    fun snapshot(): InspectionException {
        return InspectionException(
            code = code,
            name = name,
            budget = budget,
            externalRef = externalRef,
            floorValue = floorValue,
            active = active,
            label = label,
            summary = summary,
            stopCount = stopCount,
            fuelLiters = fuelLiters,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a inspection exception in status new. */
fun newInspectionException(code: String, name: String): InspectionException {
    return InspectionException(
        code = code,
        name = name,
        budget = 0.0,
        externalRef = "",
        floorValue = 0.0,
        active = false,
        label = "",
        summary = "",
        stopCount = 0,
        fuelLiters = 0.0,
        locale = "",
        status = InspectionExceptionStatus.NEW,
    )
}
