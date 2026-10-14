package com.acme.transport.customs.inspection.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection digest records of customs's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection digest is in its life cycle. */
enum class InspectionDigestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A inspection digest as the inspection team keeps it. */
data class InspectionDigest(
    var code: String,
    var name: String,
    var notes: String,
    var locale: String,
    var minimum: Double,
    var loadKg: Double,
    var active: Boolean,
    var description: String,
    var status: InspectionDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionDigestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionDigestStatus): Boolean {
        when (status) {
            InspectionDigestStatus.DRAFT -> return next == InspectionDigestStatus.CLOSED
            InspectionDigestStatus.PENDING -> return next == InspectionDigestStatus.SUSPENDED || next == InspectionDigestStatus.CLOSED
            InspectionDigestStatus.ACTIVE -> return next == InspectionDigestStatus.CLOSED
            InspectionDigestStatus.SUSPENDED -> return next == InspectionDigestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: InspectionDigestStatus) {
        if (!canMoveTo(next)) {
            error("inspection digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"inspection digest"} $code"

    fun weightedLoad(): Double = round((loadKg - minimum) * 100) / 100

    fun snapshot(): InspectionDigest {
        return InspectionDigest(
            code = code,
            name = name,
            notes = notes,
            locale = locale,
            minimum = minimum,
            loadKg = loadKg,
            active = active,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a inspection digest in status draft. */
fun newInspectionDigest(code: String, name: String): InspectionDigest {
    return InspectionDigest(
        code = code,
        name = name,
        notes = "",
        locale = "",
        minimum = 0.0,
        loadKg = 0.0,
        active = false,
        description = "",
        status = InspectionDigestStatus.DRAFT,
    )
}
