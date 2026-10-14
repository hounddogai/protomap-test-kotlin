package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tender assignment records of linehaul's tender work, and the rules, stores, and calculations around
// them.

/** Where a tender assignment is in its life cycle. */
enum class TenderAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tender assignment as the tender team keeps it. */
data class TenderAssignment(
    var code: String,
    var label: String,
    var batchSize: Int,
    var rate: Double,
    var approved: Boolean,
    var tags: MutableList<String>,
    var windowDays: Int,
    var ratio: Double,
    var locale: String,
    var status: TenderAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TenderAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TenderAssignmentStatus): Boolean {
        when (status) {
            TenderAssignmentStatus.PLANNED -> return next == TenderAssignmentStatus.COMPLETED || next == TenderAssignmentStatus.CANCELLED
            TenderAssignmentStatus.SCHEDULED -> {
                when (next) {
                    TenderAssignmentStatus.IN_PROGRESS, TenderAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            TenderAssignmentStatus.IN_PROGRESS -> {
                when (next) {
                    TenderAssignmentStatus.COMPLETED, TenderAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            TenderAssignmentStatus.COMPLETED -> {
                when (next) {
                    TenderAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TenderAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("tender assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round(batchSize.toDouble() * rate * 100) / 100

    fun snapshot(): TenderAssignment {
        return TenderAssignment(
            code = code,
            label = label,
            batchSize = batchSize,
            rate = rate,
            approved = approved,
            tags = tags,
            windowDays = windowDays,
            ratio = ratio,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tender assignment in status planned. */
fun newTenderAssignment(code: String, label: String): TenderAssignment {
    return TenderAssignment(
        code = code,
        label = label,
        batchSize = 0,
        rate = 0.0,
        approved = false,
        tags = mutableListOf<String>(),
        windowDays = 0,
        ratio = 0.0,
        locale = "",
        status = TenderAssignmentStatus.PLANNED,
    )
}
