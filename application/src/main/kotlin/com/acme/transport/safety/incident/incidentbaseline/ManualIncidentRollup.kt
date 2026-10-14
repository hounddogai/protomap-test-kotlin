package com.acme.transport.safety.incident.incidentbaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual incident rollup is in its life cycle. */
enum class ManualIncidentRollupStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One manual incident rollup, with what the planners need to act on it. */
data class ManualIncidentRollup(
    var code: String,
    var title: String,
    var unit: String,
    var tags: MutableList<String>,
    var loadKg: Double,
    var batchSize: Int,
    var distanceKm: Double,
    var startsAt: Int,
    var carrier: String,
    var weight: Double,
    var status: ManualIncidentRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualIncidentRollupStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualIncidentRollupStatus): Boolean {
        when (status) {
            ManualIncidentRollupStatus.NEW -> return next == ManualIncidentRollupStatus.DONE
            ManualIncidentRollupStatus.QUEUED -> {
                when (next) {
                    ManualIncidentRollupStatus.RUNNING -> return true
                }
                return false
            }
            ManualIncidentRollupStatus.RUNNING -> {
                when (next) {
                    ManualIncidentRollupStatus.FAILED -> return true
                }
                return false
            }
            ManualIncidentRollupStatus.DONE -> return next == ManualIncidentRollupStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ManualIncidentRollupStatus) {
        if (!canMoveTo(next)) {
            error("manual incident rollup $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedLoad(): Double = round((loadKg - batchSize.toDouble()) * 100) / 100

    fun snapshot(): ManualIncidentRollup {
        return ManualIncidentRollup(
            code = code,
            title = title,
            unit = unit,
            tags = tags,
            loadKg = loadKg,
            batchSize = batchSize,
            distanceKm = distanceKm,
            startsAt = startsAt,
            carrier = carrier,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a manual incident rollup in status new. */
fun newManualIncidentRollup(code: String, title: String): ManualIncidentRollup {
    return ManualIncidentRollup(
        code = code,
        title = title,
        unit = "",
        tags = mutableListOf<String>(),
        loadKg = 0.0,
        batchSize = 0,
        distanceKm = 0.0,
        startsAt = 0,
        carrier = "",
        weight = 0.0,
        status = ManualIncidentRollupStatus.NEW,
    )
}
