package com.acme.transport.fleetops.defect.allocation

import com.acme.transport.kit.text.normalize

/** Where a defect slot is in its life cycle. */
enum class DefectSlotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a defect slot and its current status. */
data class DefectSlot(
    var code: String,
    var label: String,
    var description: String,
    var attributes: MutableMap<String, String>,
    var ratio: Double,
    var status: DefectSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectSlotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectSlotStatus): Boolean {
        when (status) {
            DefectSlotStatus.IDLE -> {
                when (next) {
                    DefectSlotStatus.STOPPED -> return true
                }
                return false
            }
            DefectSlotStatus.WARMING -> {
                when (next) {
                    DefectSlotStatus.DRAINING, DefectSlotStatus.STOPPED -> return true
                }
                return false
            }
            DefectSlotStatus.READY -> {
                when (next) {
                    DefectSlotStatus.DRAINING, DefectSlotStatus.STOPPED -> return true
                }
                return false
            }
            DefectSlotStatus.DRAINING -> return next == DefectSlotStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefectSlotStatus) {
        if (!canMoveTo(next)) {
            error("defect slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = ratio

    fun snapshot(): DefectSlot {
        return DefectSlot(
            code = code,
            label = label,
            description = description,
            attributes = attributes,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a defect slot in status idle. */
fun newDefectSlot(code: String, label: String): DefectSlot {
    return DefectSlot(
        code = code,
        label = label,
        description = "",
        attributes = mutableMapOf<String, String>(),
        ratio = 0.0,
        status = DefectSlotStatus.IDLE,
    )
}
