package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict dwell slot is in its life cycle. */
enum class StrictDwellSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict dwell slot. Its code is unique within a tenant. */
data class StrictDwellSlot(
    var code: String,
    var label: String,
    var dock: String,
    var tags: MutableList<String>,
    var approved: Boolean,
    var amount: Double,
    var threshold: Double,
    var carrier: String,
    var score: Double,
    var attempts: Int,
    var priority: Int,
    var status: StrictDwellSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDwellSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDwellSlotStatus): Boolean {
        when (status) {
            StrictDwellSlotStatus.NEW -> {
                when (next) {
                    StrictDwellSlotStatus.RUNNING -> return true
                }
                return false
            }
            StrictDwellSlotStatus.QUEUED -> return next == StrictDwellSlotStatus.DONE
            StrictDwellSlotStatus.RUNNING -> {
                when (next) {
                    StrictDwellSlotStatus.DONE, StrictDwellSlotStatus.FAILED -> return true
                }
                return false
            }
            StrictDwellSlotStatus.DONE -> return next == StrictDwellSlotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictDwellSlotStatus) {
        if (!canMoveTo(next)) {
            error("strict dwell slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"strict dwell slot"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round(threshold * priority.toDouble() * 100) / 100

    fun snapshot(): StrictDwellSlot {
        return StrictDwellSlot(
            code = code,
            label = label,
            dock = dock,
            tags = tags,
            approved = approved,
            amount = amount,
            threshold = threshold,
            carrier = carrier,
            score = score,
            attempts = attempts,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict dwell slot in status new. */
fun newStrictDwellSlot(code: String, label: String): StrictDwellSlot {
    return StrictDwellSlot(
        code = code,
        label = label,
        dock = "",
        tags = mutableListOf<String>(),
        approved = false,
        amount = 0.0,
        threshold = 0.0,
        carrier = "",
        score = 0.0,
        attempts = 0,
        priority = 0,
        status = StrictDwellSlotStatus.NEW,
    )
}
