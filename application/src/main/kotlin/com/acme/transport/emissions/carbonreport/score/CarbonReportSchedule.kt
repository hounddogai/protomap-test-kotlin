package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report schedule is in its life cycle. */
enum class CarbonReportScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a carbon report schedule and its current status. */
data class CarbonReportSchedule(
    var code: String,
    var name: String,
    var maximum: Double,
    var score: Double,
    var description: String,
    var pallets: Int,
    var fuelLiters: Double,
    var phone: String,
    var status: CarbonReportScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportScheduleStatus): Boolean {
        when (status) {
            CarbonReportScheduleStatus.NEW -> {
                when (next) {
                    CarbonReportScheduleStatus.RUNNING, CarbonReportScheduleStatus.FAILED -> return true
                }
                return false
            }
            CarbonReportScheduleStatus.QUEUED -> return next == CarbonReportScheduleStatus.DONE || next == CarbonReportScheduleStatus.FAILED
            CarbonReportScheduleStatus.RUNNING -> {
                when (next) {
                    CarbonReportScheduleStatus.DONE, CarbonReportScheduleStatus.FAILED -> return true
                }
                return false
            }
            CarbonReportScheduleStatus.DONE -> {
                when (next) {
                    CarbonReportScheduleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportScheduleStatus) {
        if (!canMoveTo(next)) {
            error("carbon report schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedValue(): Double = round((maximum - pallets.toDouble()) * 100) / 100

    fun snapshot(): CarbonReportSchedule {
        return CarbonReportSchedule(
            code = code,
            name = name,
            maximum = maximum,
            score = score,
            description = description,
            pallets = pallets,
            fuelLiters = fuelLiters,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a carbon report schedule in status new. */
fun newCarbonReportSchedule(code: String, name: String): CarbonReportSchedule {
    return CarbonReportSchedule(
        code = code,
        name = name,
        maximum = 0.0,
        score = 0.0,
        description = "",
        pallets = 0,
        fuelLiters = 0.0,
        phone = "",
        status = CarbonReportScheduleStatus.NEW,
    )
}
