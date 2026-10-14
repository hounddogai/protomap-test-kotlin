package com.acme.transport.customs.inspection.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default inspection mapping records of customs's inspection work, and the rules, stores, and
// calculations around them.

/** Where a default inspection mapping is in its life cycle. */
enum class DefaultInspectionMappingStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default inspection mapping. Its code is unique within a tenant. */
data class DefaultInspectionMapping(
    var code: String,
    var label: String,
    var reviewer: String,
    var sequence: Int,
    var durationMinutes: Int,
    var status: DefaultInspectionMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultInspectionMappingStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultInspectionMappingStatus): Boolean {
        when (status) {
            DefaultInspectionMappingStatus.IDLE -> return next == DefaultInspectionMappingStatus.DRAINING
            DefaultInspectionMappingStatus.WARMING -> {
                when (next) {
                    DefaultInspectionMappingStatus.READY, DefaultInspectionMappingStatus.STOPPED -> return true
                }
                return false
            }
            DefaultInspectionMappingStatus.READY -> return next == DefaultInspectionMappingStatus.DRAINING
            DefaultInspectionMappingStatus.DRAINING -> {
                when (next) {
                    DefaultInspectionMappingStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultInspectionMappingStatus) {
        if (!canMoveTo(next)) {
            error("default inspection mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round((durationMinutes.toDouble() + sequence.toDouble()) * 100) / 100

    fun snapshot(): DefaultInspectionMapping {
        return DefaultInspectionMapping(
            code = code,
            label = label,
            reviewer = reviewer,
            sequence = sequence,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default inspection mapping in status idle. */
fun newDefaultInspectionMapping(code: String, label: String): DefaultInspectionMapping {
    return DefaultInspectionMapping(
        code = code,
        label = label,
        reviewer = "",
        sequence = 0,
        durationMinutes = 0,
        status = DefaultInspectionMappingStatus.IDLE,
    )
}
