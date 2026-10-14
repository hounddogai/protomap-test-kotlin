package com.acme.transport.customs.declaration.declarationsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal declaration assignment is in its life cycle. */
enum class SeasonalDeclarationAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal declaration assignment as the declaration team keeps it. */
data class SeasonalDeclarationAssignment(
    var code: String,
    var name: String,
    var odometer: Int,
    var contactEmail: String,
    var batchSize: Int,
    var windowDays: Int,
    var status: SeasonalDeclarationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalDeclarationAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalDeclarationAssignmentStatus): Boolean {
        when (status) {
            SeasonalDeclarationAssignmentStatus.DRAFT -> return next == SeasonalDeclarationAssignmentStatus.CLOSED
            SeasonalDeclarationAssignmentStatus.PENDING -> return next == SeasonalDeclarationAssignmentStatus.ACTIVE || next == SeasonalDeclarationAssignmentStatus.SUSPENDED
            SeasonalDeclarationAssignmentStatus.ACTIVE -> return next == SeasonalDeclarationAssignmentStatus.SUSPENDED
            SeasonalDeclarationAssignmentStatus.SUSPENDED -> return next == SeasonalDeclarationAssignmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SeasonalDeclarationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal declaration assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round((windowDays.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): SeasonalDeclarationAssignment {
        return SeasonalDeclarationAssignment(
            code = code,
            name = name,
            odometer = odometer,
            contactEmail = contactEmail,
            batchSize = batchSize,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal declaration assignment in status draft. */
fun newSeasonalDeclarationAssignment(code: String, name: String): SeasonalDeclarationAssignment {
    return SeasonalDeclarationAssignment(
        code = code,
        name = name,
        odometer = 0,
        contactEmail = "",
        batchSize = 0,
        windowDays = 0,
        status = SeasonalDeclarationAssignmentStatus.DRAFT,
    )
}
