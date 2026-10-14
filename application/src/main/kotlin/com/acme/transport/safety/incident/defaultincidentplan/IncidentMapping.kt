package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident mapping is in its life cycle. */
enum class IncidentMappingStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A incident mapping as the incident team keeps it. */
data class IncidentMapping(
    var code: String,
    var label: String,
    var externalRef: String,
    var country: String,
    var limit: Int,
    var cost: Double,
    var quantity: Int,
    var name: String,
    var batchSize: Int,
    var channel: String,
    var status: IncidentMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentMappingStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentMappingStatus): Boolean {
        when (status) {
            IncidentMappingStatus.NEW -> return next == IncidentMappingStatus.QUEUED
            IncidentMappingStatus.QUEUED -> return next == IncidentMappingStatus.FAILED
            IncidentMappingStatus.RUNNING -> {
                when (next) {
                    IncidentMappingStatus.DONE -> return true
                }
                return false
            }
            IncidentMappingStatus.DONE -> return next == IncidentMappingStatus.FAILED
        }
        return false
    }

    fun moveTo(next: IncidentMappingStatus) {
        if (!canMoveTo(next)) {
            error("incident mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun weightedExposure(): Double = round((limit.toDouble() + cost) * 100) / 100

    fun snapshot(): IncidentMapping {
        return IncidentMapping(
            code = code,
            label = label,
            externalRef = externalRef,
            country = country,
            limit = limit,
            cost = cost,
            quantity = quantity,
            name = name,
            batchSize = batchSize,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a incident mapping in status new. */
fun newIncidentMapping(code: String, label: String): IncidentMapping {
    return IncidentMapping(
        code = code,
        label = label,
        externalRef = "",
        country = "",
        limit = 0,
        cost = 0.0,
        quantity = 0,
        name = "",
        batchSize = 0,
        channel = "",
        status = IncidentMappingStatus.NEW,
    )
}
