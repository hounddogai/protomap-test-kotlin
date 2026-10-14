package com.acme.transport.terminal.dwell.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell group is in its life cycle. */
enum class DwellGroupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a dwell group and its current status. */
data class DwellGroup(
    var code: String,
    var name: String,
    var batchSize: Int,
    var attributes: MutableMap<String, String>,
    var margin: Double,
    var stopCount: Int,
    var tags: MutableList<String>,
    var currency: String,
    var carrier: String,
    var status: DwellGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellGroupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellGroupStatus): Boolean {
        when (status) {
            DwellGroupStatus.CREATED -> {
                when (next) {
                    DwellGroupStatus.PUBLISHED -> return true
                }
                return false
            }
            DwellGroupStatus.VALIDATED -> return next == DwellGroupStatus.PUBLISHED
            DwellGroupStatus.PUBLISHED -> {
                when (next) {
                    DwellGroupStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellGroupStatus) {
        if (!canMoveTo(next)) {
            error("dwell group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveVolume(): Double = round((stopCount.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): DwellGroup {
        return DwellGroup(
            code = code,
            name = name,
            batchSize = batchSize,
            attributes = attributes,
            margin = margin,
            stopCount = stopCount,
            tags = tags,
            currency = currency,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dwell group in status created. */
fun newDwellGroup(code: String, name: String): DwellGroup {
    return DwellGroup(
        code = code,
        name = name,
        batchSize = 0,
        attributes = mutableMapOf<String, String>(),
        margin = 0.0,
        stopCount = 0,
        tags = mutableListOf<String>(),
        currency = "",
        carrier = "",
        status = DwellGroupStatus.CREATED,
    )
}
