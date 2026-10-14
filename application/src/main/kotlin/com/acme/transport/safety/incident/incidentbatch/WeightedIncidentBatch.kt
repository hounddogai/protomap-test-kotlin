package com.acme.transport.safety.incident.incidentbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted incident batch records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a weighted incident batch is in its life cycle. */
enum class WeightedIncidentBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted incident batch as the incident team keeps it. */
data class WeightedIncidentBatch(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var sequence: Int,
    var pinned: Boolean,
    var quantity: Int,
    var margin: Double,
    var status: WeightedIncidentBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedIncidentBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedIncidentBatchStatus): Boolean {
        when (status) {
            WeightedIncidentBatchStatus.DRAFT -> return next == WeightedIncidentBatchStatus.CLOSED
            WeightedIncidentBatchStatus.PENDING -> {
                when (next) {
                    WeightedIncidentBatchStatus.ACTIVE, WeightedIncidentBatchStatus.CLOSED -> return true
                }
                return false
            }
            WeightedIncidentBatchStatus.ACTIVE -> {
                when (next) {
                    WeightedIncidentBatchStatus.SUSPENDED, WeightedIncidentBatchStatus.CLOSED -> return true
                }
                return false
            }
            WeightedIncidentBatchStatus.SUSPENDED -> {
                when (next) {
                    WeightedIncidentBatchStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedIncidentBatchStatus) {
        if (!canMoveTo(next)) {
            error("weighted incident batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"weighted incident batch"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round((margin + sequence.toDouble()) * 100) / 100

    fun snapshot(): WeightedIncidentBatch {
        return WeightedIncidentBatch(
            code = code,
            label = label,
            attributes = attributes,
            sequence = sequence,
            pinned = pinned,
            quantity = quantity,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted incident batch in status draft. */
fun newWeightedIncidentBatch(code: String, label: String): WeightedIncidentBatch {
    return WeightedIncidentBatch(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        sequence = 0,
        pinned = false,
        quantity = 0,
        margin = 0.0,
        status = WeightedIncidentBatchStatus.DRAFT,
    )
}
