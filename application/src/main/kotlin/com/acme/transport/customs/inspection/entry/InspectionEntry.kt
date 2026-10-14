package com.acme.transport.customs.inspection.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection entry records of customs's inspection work, and the rules, stores, and calculations around
// them.

/** Where a inspection entry is in its life cycle. */
enum class InspectionEntryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A inspection entry as the inspection team keeps it. */
data class InspectionEntry(
    var code: String,
    var title: String,
    var contactEmail: String,
    var name: String,
    var attempts: Int,
    var stopCount: Int,
    var etaMinutes: Int,
    var locale: String,
    var revision: Int,
    var trailerId: String,
    var graceDays: Int,
    var status: InspectionEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionEntryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionEntryStatus): Boolean {
        when (status) {
            InspectionEntryStatus.DRAFT -> {
                when (next) {
                    InspectionEntryStatus.PENDING, InspectionEntryStatus.CLOSED -> return true
                }
                return false
            }
            InspectionEntryStatus.PENDING -> return next == InspectionEntryStatus.SUSPENDED || next == InspectionEntryStatus.CLOSED
            InspectionEntryStatus.ACTIVE -> {
                when (next) {
                    InspectionEntryStatus.SUSPENDED -> return true
                }
                return false
            }
            InspectionEntryStatus.SUSPENDED -> {
                when (next) {
                    InspectionEntryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionEntryStatus) {
        if (!canMoveTo(next)) {
            error("inspection entry $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"inspection entry"} $code"

    fun effectiveExposure(): Double = round((graceDays.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): InspectionEntry {
        return InspectionEntry(
            code = code,
            title = title,
            contactEmail = contactEmail,
            name = name,
            attempts = attempts,
            stopCount = stopCount,
            etaMinutes = etaMinutes,
            locale = locale,
            revision = revision,
            trailerId = trailerId,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a inspection entry in status draft. */
fun newInspectionEntry(code: String, title: String): InspectionEntry {
    return InspectionEntry(
        code = code,
        title = title,
        contactEmail = "",
        name = "",
        attempts = 0,
        stopCount = 0,
        etaMinutes = 0,
        locale = "",
        revision = 0,
        trailerId = "",
        graceDays = 0,
        status = InspectionEntryStatus.DRAFT,
    )
}
