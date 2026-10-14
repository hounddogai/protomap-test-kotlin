package com.acme.transport.fleetops.defect.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional defect threshold is in its life cycle. */
enum class RegionalDefectThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One regional defect threshold, with what the planners need to act on it. */
data class RegionalDefectThreshold(
    var code: String,
    var title: String,
    var windowDays: Int,
    var updatedAt: Int,
    var attempts: Int,
    var loadKg: Double,
    var externalRef: String,
    var discount: Double,
    var summary: String,
    var graceDays: Int,
    var shiftCode: String,
    var status: RegionalDefectThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDefectThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDefectThresholdStatus): Boolean {
        when (status) {
            RegionalDefectThresholdStatus.NEW -> {
                when (next) {
                    RegionalDefectThresholdStatus.RUNNING -> return true
                }
                return false
            }
            RegionalDefectThresholdStatus.QUEUED -> return next == RegionalDefectThresholdStatus.FAILED
            RegionalDefectThresholdStatus.RUNNING -> return next == RegionalDefectThresholdStatus.DONE || next == RegionalDefectThresholdStatus.FAILED
            RegionalDefectThresholdStatus.DONE -> return next == RegionalDefectThresholdStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalDefectThresholdStatus) {
        if (!canMoveTo(next)) {
            error("regional defect threshold $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netValue(): Double = round((discount + attempts.toDouble()) * 100) / 100

    fun snapshot(): RegionalDefectThreshold {
        return RegionalDefectThreshold(
            code = code,
            title = title,
            windowDays = windowDays,
            updatedAt = updatedAt,
            attempts = attempts,
            loadKg = loadKg,
            externalRef = externalRef,
            discount = discount,
            summary = summary,
            graceDays = graceDays,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional defect threshold in status new. */
fun newRegionalDefectThreshold(code: String, title: String): RegionalDefectThreshold {
    return RegionalDefectThreshold(
        code = code,
        title = title,
        windowDays = 0,
        updatedAt = 0,
        attempts = 0,
        loadKg = 0.0,
        externalRef = "",
        discount = 0.0,
        summary = "",
        graceDays = 0,
        shiftCode = "",
        status = RegionalDefectThresholdStatus.NEW,
    )
}
