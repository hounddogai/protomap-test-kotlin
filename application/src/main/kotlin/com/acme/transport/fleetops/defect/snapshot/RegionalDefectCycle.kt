package com.acme.transport.fleetops.defect.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional defect cycle is in its life cycle. */
enum class RegionalDefectCycleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a regional defect cycle and its current status. */
data class RegionalDefectCycle(
    var code: String,
    var label: String,
    var active: Boolean,
    var weight: Double,
    var limit: Int,
    var phone: String,
    var margin: Double,
    var score: Double,
    var reviewer: String,
    var timezone: String,
    var maximum: Double,
    var status: RegionalDefectCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDefectCycleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDefectCycleStatus): Boolean {
        when (status) {
            RegionalDefectCycleStatus.REQUESTED -> {
                when (next) {
                    RegionalDefectCycleStatus.EXPIRED -> return true
                }
                return false
            }
            RegionalDefectCycleStatus.GRANTED -> {
                when (next) {
                    RegionalDefectCycleStatus.REVOKED -> return true
                }
                return false
            }
            RegionalDefectCycleStatus.REVOKED -> {
                when (next) {
                    RegionalDefectCycleStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDefectCycleStatus) {
        if (!canMoveTo(next)) {
            error("regional defect cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round((margin + weight) * 100) / 100

    fun snapshot(): RegionalDefectCycle {
        return RegionalDefectCycle(
            code = code,
            label = label,
            active = active,
            weight = weight,
            limit = limit,
            phone = phone,
            margin = margin,
            score = score,
            reviewer = reviewer,
            timezone = timezone,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional defect cycle in status requested. */
fun newRegionalDefectCycle(code: String, label: String): RegionalDefectCycle {
    return RegionalDefectCycle(
        code = code,
        label = label,
        active = false,
        weight = 0.0,
        limit = 0,
        phone = "",
        margin = 0.0,
        score = 0.0,
        reviewer = "",
        timezone = "",
        maximum = 0.0,
        status = RegionalDefectCycleStatus.REQUESTED,
    )
}
