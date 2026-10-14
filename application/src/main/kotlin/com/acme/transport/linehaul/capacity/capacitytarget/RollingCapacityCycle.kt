package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.kit.text.normalize

/** Where a rolling capacity cycle is in its life cycle. */
enum class RollingCapacityCycleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One rolling capacity cycle, with what the planners need to act on it. */
data class RollingCapacityCycle(
    var code: String,
    var label: String,
    var ceiling: Double,
    var visible: Boolean,
    var contactEmail: String,
    var region: String,
    var status: RollingCapacityCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCapacityCycleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCapacityCycleStatus): Boolean {
        when (status) {
            RollingCapacityCycleStatus.NEW -> {
                when (next) {
                    RollingCapacityCycleStatus.RUNNING, RollingCapacityCycleStatus.DONE -> return true
                }
                return false
            }
            RollingCapacityCycleStatus.QUEUED -> {
                when (next) {
                    RollingCapacityCycleStatus.RUNNING, RollingCapacityCycleStatus.FAILED -> return true
                }
                return false
            }
            RollingCapacityCycleStatus.RUNNING -> {
                when (next) {
                    RollingCapacityCycleStatus.DONE, RollingCapacityCycleStatus.FAILED -> return true
                }
                return false
            }
            RollingCapacityCycleStatus.DONE -> {
                when (next) {
                    RollingCapacityCycleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingCapacityCycleStatus) {
        if (!canMoveTo(next)) {
            error("rolling capacity cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = ceiling

    fun snapshot(): RollingCapacityCycle {
        return RollingCapacityCycle(
            code = code,
            label = label,
            ceiling = ceiling,
            visible = visible,
            contactEmail = contactEmail,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling capacity cycle in status new. */
fun newRollingCapacityCycle(code: String, label: String): RollingCapacityCycle {
    return RollingCapacityCycle(
        code = code,
        label = label,
        ceiling = 0.0,
        visible = false,
        contactEmail = "",
        region = "",
        status = RollingCapacityCycleStatus.NEW,
    )
}
