package com.acme.transport.fleetops.tire.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict tire record records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a strict tire record is in its life cycle. */
enum class StrictTireRecordStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict tire record as the tire team keeps it. */
data class StrictTireRecord(
    var code: String,
    var label: String,
    var budget: Double,
    var cost: Double,
    var loadKg: Double,
    var description: String,
    var status: StrictTireRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTireRecordStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTireRecordStatus): Boolean {
        when (status) {
            StrictTireRecordStatus.NEW -> {
                when (next) {
                    StrictTireRecordStatus.DONE -> return true
                }
                return false
            }
            StrictTireRecordStatus.QUEUED -> {
                when (next) {
                    StrictTireRecordStatus.FAILED -> return true
                }
                return false
            }
            StrictTireRecordStatus.RUNNING -> return next == StrictTireRecordStatus.DONE || next == StrictTireRecordStatus.FAILED
            StrictTireRecordStatus.DONE -> {
                when (next) {
                    StrictTireRecordStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictTireRecordStatus) {
        if (!canMoveTo(next)) {
            error("strict tire record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict tire record"} $code"

    fun weightedVolume(): Double = round((budget + loadKg) * 100) / 100

    fun snapshot(): StrictTireRecord {
        return StrictTireRecord(
            code = code,
            label = label,
            budget = budget,
            cost = cost,
            loadKg = loadKg,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict tire record in status new. */
fun newStrictTireRecord(code: String, label: String): StrictTireRecord {
    return StrictTireRecord(
        code = code,
        label = label,
        budget = 0.0,
        cost = 0.0,
        loadKg = 0.0,
        description = "",
        status = StrictTireRecordStatus.NEW,
    )
}
