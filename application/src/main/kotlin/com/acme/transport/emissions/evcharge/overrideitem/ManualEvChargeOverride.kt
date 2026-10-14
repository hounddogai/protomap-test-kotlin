package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.kit.text.normalize

// The manual ev charge override records of emissions's ev charge work, and the rules, stores, and
// calculations around them.

/** Where a manual ev charge override is in its life cycle. */
enum class ManualEvChargeOverrideStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A manual ev charge override. Its code is unique within a tenant. */
data class ManualEvChargeOverride(
    var code: String,
    var label: String,
    var category: String,
    var shiftCode: String,
    var summary: String,
    var ceiling: Double,
    var status: ManualEvChargeOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualEvChargeOverrideStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualEvChargeOverrideStatus): Boolean {
        when (status) {
            ManualEvChargeOverrideStatus.NEW -> {
                when (next) {
                    ManualEvChargeOverrideStatus.FAILED -> return true
                }
                return false
            }
            ManualEvChargeOverrideStatus.QUEUED -> {
                when (next) {
                    ManualEvChargeOverrideStatus.RUNNING, ManualEvChargeOverrideStatus.DONE -> return true
                }
                return false
            }
            ManualEvChargeOverrideStatus.RUNNING -> {
                when (next) {
                    ManualEvChargeOverrideStatus.FAILED -> return true
                }
                return false
            }
            ManualEvChargeOverrideStatus.DONE -> {
                when (next) {
                    ManualEvChargeOverrideStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualEvChargeOverrideStatus) {
        if (!canMoveTo(next)) {
            error("manual ev charge override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = ceiling

    fun snapshot(): ManualEvChargeOverride {
        return ManualEvChargeOverride(
            code = code,
            label = label,
            category = category,
            shiftCode = shiftCode,
            summary = summary,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a manual ev charge override in status new. */
fun newManualEvChargeOverride(code: String, label: String): ManualEvChargeOverride {
    return ManualEvChargeOverride(
        code = code,
        label = label,
        category = "",
        shiftCode = "",
        summary = "",
        ceiling = 0.0,
        status = ManualEvChargeOverrideStatus.NEW,
    )
}
