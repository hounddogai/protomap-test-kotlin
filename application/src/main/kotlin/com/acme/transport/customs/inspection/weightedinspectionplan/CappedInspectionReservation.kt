package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped inspection reservation is in its life cycle. */
enum class CappedInspectionReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped inspection reservation. Its code is unique within a tenant. */
data class CappedInspectionReservation(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var batchSize: Int,
    var category: String,
    var endsAt: Int,
    var attributes: MutableMap<String, String>,
    var status: CappedInspectionReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedInspectionReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedInspectionReservationStatus): Boolean {
        when (status) {
            CappedInspectionReservationStatus.PROPOSED -> return next == CappedInspectionReservationStatus.WITHDRAWN
            CappedInspectionReservationStatus.APPROVED -> {
                when (next) {
                    CappedInspectionReservationStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedInspectionReservationStatus.REJECTED -> return next == CappedInspectionReservationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedInspectionReservationStatus) {
        if (!canMoveTo(next)) {
            error("capped inspection reservation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

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

    fun weightedTotal(): Double = round((endsAt.toDouble() + batchSize.toDouble()) * 100) / 100

    fun snapshot(): CappedInspectionReservation {
        return CappedInspectionReservation(
            code = code,
            title = title,
            tags = tags,
            batchSize = batchSize,
            category = category,
            endsAt = endsAt,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped inspection reservation in status proposed. */
fun newCappedInspectionReservation(code: String, title: String): CappedInspectionReservation {
    return CappedInspectionReservation(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        batchSize = 0,
        category = "",
        endsAt = 0,
        attributes = mutableMapOf<String, String>(),
        status = CappedInspectionReservationStatus.PROPOSED,
    )
}
