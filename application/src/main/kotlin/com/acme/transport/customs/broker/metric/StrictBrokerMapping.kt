package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict broker mapping is in its life cycle. */
enum class StrictBrokerMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a strict broker mapping and its current status. */
data class StrictBrokerMapping(
    var code: String,
    var label: String,
    var version: Int,
    var summary: String,
    var limit: Int,
    var segment: String,
    var unit: String,
    var status: StrictBrokerMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictBrokerMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictBrokerMappingStatus): Boolean {
        when (status) {
            StrictBrokerMappingStatus.CREATED -> {
                when (next) {
                    StrictBrokerMappingStatus.RETIRED -> return true
                }
                return false
            }
            StrictBrokerMappingStatus.VALIDATED -> {
                when (next) {
                    StrictBrokerMappingStatus.PUBLISHED, StrictBrokerMappingStatus.RETIRED -> return true
                }
                return false
            }
            StrictBrokerMappingStatus.PUBLISHED -> {
                when (next) {
                    StrictBrokerMappingStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictBrokerMappingStatus) {
        if (!canMoveTo(next)) {
            error("strict broker mapping $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedValue(): Double = round((version.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): StrictBrokerMapping {
        return StrictBrokerMapping(
            code = code,
            label = label,
            version = version,
            summary = summary,
            limit = limit,
            segment = segment,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a strict broker mapping in status created. */
fun newStrictBrokerMapping(code: String, label: String): StrictBrokerMapping {
    return StrictBrokerMapping(
        code = code,
        label = label,
        version = 0,
        summary = "",
        limit = 0,
        segment = "",
        unit = "",
        status = StrictBrokerMappingStatus.CREATED,
    )
}
