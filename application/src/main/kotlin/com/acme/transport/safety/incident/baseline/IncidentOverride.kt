package com.acme.transport.safety.incident.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a incident override is in its life cycle. */
enum class IncidentOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A incident override. Its code is unique within a tenant. */
data class IncidentOverride(
    var code: String,
    var title: String,
    var notes: String,
    var shiftCode: String,
    var amount: Double,
    var batchSize: Int,
    var attributes: MutableMap<String, String>,
    var status: IncidentOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentOverrideStatus): Boolean {
        when (status) {
            IncidentOverrideStatus.CREATED -> return next == IncidentOverrideStatus.RETIRED
            IncidentOverrideStatus.VALIDATED -> return next == IncidentOverrideStatus.RETIRED
            IncidentOverrideStatus.PUBLISHED -> return next == IncidentOverrideStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: IncidentOverrideStatus) {
        if (!canMoveTo(next)) {
            error("incident override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round((amount + batchSize.toDouble()) * 100) / 100

    fun snapshot(): IncidentOverride {
        return IncidentOverride(
            code = code,
            title = title,
            notes = notes,
            shiftCode = shiftCode,
            amount = amount,
            batchSize = batchSize,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a incident override in status created. */
fun newIncidentOverride(code: String, title: String): IncidentOverride {
    return IncidentOverride(
        code = code,
        title = title,
        notes = "",
        shiftCode = "",
        amount = 0.0,
        batchSize = 0,
        attributes = mutableMapOf<String, String>(),
        status = IncidentOverrideStatus.CREATED,
    )
}
