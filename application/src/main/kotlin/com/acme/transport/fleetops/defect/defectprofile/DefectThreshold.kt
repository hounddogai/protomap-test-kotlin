package com.acme.transport.fleetops.defect.defectprofile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect threshold is in its life cycle. */
enum class DefectThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a defect threshold and its current status. */
data class DefectThreshold(
    var code: String,
    var title: String,
    var pallets: Int,
    var region: String,
    var retries: Int,
    var summary: String,
    var windowDays: Int,
    var visible: Boolean,
    var approved: Boolean,
    var etaMinutes: Int,
    var pinned: Boolean,
    var status: DefectThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectThresholdStatus): Boolean {
        when (status) {
            DefectThresholdStatus.NEW -> {
                when (next) {
                    DefectThresholdStatus.FAILED -> return true
                }
                return false
            }
            DefectThresholdStatus.QUEUED -> return next == DefectThresholdStatus.RUNNING || next == DefectThresholdStatus.FAILED
            DefectThresholdStatus.RUNNING -> return next == DefectThresholdStatus.DONE || next == DefectThresholdStatus.FAILED
            DefectThresholdStatus.DONE -> return next == DefectThresholdStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefectThresholdStatus) {
        if (!canMoveTo(next)) {
            error("defect threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedVolume(): Double = round((pallets.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): DefectThreshold {
        return DefectThreshold(
            code = code,
            title = title,
            pallets = pallets,
            region = region,
            retries = retries,
            summary = summary,
            windowDays = windowDays,
            visible = visible,
            approved = approved,
            etaMinutes = etaMinutes,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a defect threshold in status new. */
fun newDefectThreshold(code: String, title: String): DefectThreshold {
    return DefectThreshold(
        code = code,
        title = title,
        pallets = 0,
        region = "",
        retries = 0,
        summary = "",
        windowDays = 0,
        visible = false,
        approved = false,
        etaMinutes = 0,
        pinned = false,
        status = DefectThresholdStatus.NEW,
    )
}
