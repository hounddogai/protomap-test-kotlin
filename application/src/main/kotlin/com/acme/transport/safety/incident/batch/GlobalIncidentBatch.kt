package com.acme.transport.safety.incident.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global incident batch records of safety's incident work, and the rules, stores, and calculations around
// them.

/** Where a global incident batch is in its life cycle. */
enum class GlobalIncidentBatchStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a global incident batch and its current status. */
data class GlobalIncidentBatch(
    var code: String,
    var title: String,
    var loadKg: Double,
    var limit: Int,
    var tags: MutableList<String>,
    var rate: Double,
    var reviewer: String,
    var threshold: Double,
    var timezone: String,
    var status: GlobalIncidentBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalIncidentBatchStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalIncidentBatchStatus): Boolean {
        when (status) {
            GlobalIncidentBatchStatus.CREATED -> {
                when (next) {
                    GlobalIncidentBatchStatus.RETIRED -> return true
                }
                return false
            }
            GlobalIncidentBatchStatus.VALIDATED -> return next == GlobalIncidentBatchStatus.RETIRED
            GlobalIncidentBatchStatus.PUBLISHED -> return next == GlobalIncidentBatchStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalIncidentBatchStatus) {
        if (!canMoveTo(next)) {
            error("global incident batch $code cannot move from $status to $next")
        }
        status = next
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

    fun projectedExposure(): Double = round((loadKg - limit.toDouble()) * 100) / 100

    fun snapshot(): GlobalIncidentBatch {
        return GlobalIncidentBatch(
            code = code,
            title = title,
            loadKg = loadKg,
            limit = limit,
            tags = tags,
            rate = rate,
            reviewer = reviewer,
            threshold = threshold,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global incident batch in status created. */
fun newGlobalIncidentBatch(code: String, title: String): GlobalIncidentBatch {
    return GlobalIncidentBatch(
        code = code,
        title = title,
        loadKg = 0.0,
        limit = 0,
        tags = mutableListOf<String>(),
        rate = 0.0,
        reviewer = "",
        threshold = 0.0,
        timezone = "",
        status = GlobalIncidentBatchStatus.CREATED,
    )
}
