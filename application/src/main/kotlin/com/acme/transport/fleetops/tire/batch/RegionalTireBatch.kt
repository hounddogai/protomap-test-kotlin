package com.acme.transport.fleetops.tire.batch

import com.acme.transport.kit.text.normalize

// The regional tire batch records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a regional tire batch is in its life cycle. */
enum class RegionalTireBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A regional tire batch. Its code is unique within a tenant. */
data class RegionalTireBatch(
    var code: String,
    var label: String,
    var title: String,
    var weight: Double,
    var region: String,
    var attributes: MutableMap<String, String>,
    var unit: String,
    var status: RegionalTireBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTireBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTireBatchStatus): Boolean {
        when (status) {
            RegionalTireBatchStatus.NEW -> return next == RegionalTireBatchStatus.RUNNING
            RegionalTireBatchStatus.QUEUED -> {
                when (next) {
                    RegionalTireBatchStatus.DONE, RegionalTireBatchStatus.FAILED -> return true
                }
                return false
            }
            RegionalTireBatchStatus.RUNNING -> return next == RegionalTireBatchStatus.FAILED
            RegionalTireBatchStatus.DONE -> return next == RegionalTireBatchStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RegionalTireBatchStatus) {
        if (!canMoveTo(next)) {
            error("regional tire batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = weight

    fun snapshot(): RegionalTireBatch {
        return RegionalTireBatch(
            code = code,
            label = label,
            title = title,
            weight = weight,
            region = region,
            attributes = attributes,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a regional tire batch in status new. */
fun newRegionalTireBatch(code: String, label: String): RegionalTireBatch {
    return RegionalTireBatch(
        code = code,
        label = label,
        title = "",
        weight = 0.0,
        region = "",
        attributes = mutableMapOf<String, String>(),
        unit = "",
        status = RegionalTireBatchStatus.NEW,
    )
}
