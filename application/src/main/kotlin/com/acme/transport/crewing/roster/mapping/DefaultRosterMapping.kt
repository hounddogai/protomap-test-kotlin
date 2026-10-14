package com.acme.transport.crewing.roster.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default roster mapping records of crewing's roster work, and the rules, stores, and calculations around
// them.

/** Where a default roster mapping is in its life cycle. */
enum class DefaultRosterMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A default roster mapping. Its code is unique within a tenant. */
data class DefaultRosterMapping(
    var code: String,
    var label: String,
    var rate: Double,
    var phone: String,
    var odometer: Int,
    var summary: String,
    var ownerId: String,
    var status: DefaultRosterMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRosterMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRosterMappingStatus): Boolean {
        when (status) {
            DefaultRosterMappingStatus.NEW -> return next == DefaultRosterMappingStatus.DONE
            DefaultRosterMappingStatus.QUEUED -> return next == DefaultRosterMappingStatus.DONE || next == DefaultRosterMappingStatus.FAILED
            DefaultRosterMappingStatus.RUNNING -> {
                when (next) {
                    DefaultRosterMappingStatus.DONE, DefaultRosterMappingStatus.FAILED -> return true
                }
                return false
            }
            DefaultRosterMappingStatus.DONE -> return next == DefaultRosterMappingStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefaultRosterMappingStatus) {
        if (!canMoveTo(next)) {
            error("default roster mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round((odometer.toDouble() + rate) * 100) / 100

    fun snapshot(): DefaultRosterMapping {
        return DefaultRosterMapping(
            code = code,
            label = label,
            rate = rate,
            phone = phone,
            odometer = odometer,
            summary = summary,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default roster mapping in status new. */
fun newDefaultRosterMapping(code: String, label: String): DefaultRosterMapping {
    return DefaultRosterMapping(
        code = code,
        label = label,
        rate = 0.0,
        phone = "",
        odometer = 0,
        summary = "",
        ownerId = "",
        status = DefaultRosterMappingStatus.NEW,
    )
}
