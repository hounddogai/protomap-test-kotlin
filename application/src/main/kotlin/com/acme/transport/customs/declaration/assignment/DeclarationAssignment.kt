package com.acme.transport.customs.declaration.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The declaration assignment records of customs's declaration work, and the rules, stores, and calculations
// around them.

/** Where a declaration assignment is in its life cycle. */
enum class DeclarationAssignmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a declaration assignment and its current status. */
data class DeclarationAssignment(
    var code: String,
    var title: String,
    var windowDays: Int,
    var category: String,
    var ratio: Double,
    var revision: Int,
    var version: Int,
    var odometer: Int,
    var depot: String,
    var loadKg: Double,
    var status: DeclarationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationAssignmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationAssignmentStatus): Boolean {
        when (status) {
            DeclarationAssignmentStatus.IDLE -> return next == DeclarationAssignmentStatus.DRAINING || next == DeclarationAssignmentStatus.STOPPED
            DeclarationAssignmentStatus.WARMING -> {
                when (next) {
                    DeclarationAssignmentStatus.STOPPED -> return true
                }
                return false
            }
            DeclarationAssignmentStatus.READY -> return next == DeclarationAssignmentStatus.DRAINING
            DeclarationAssignmentStatus.DRAINING -> {
                when (next) {
                    DeclarationAssignmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DeclarationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("declaration assignment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun effectiveTotal(): Double = round((revision.toDouble() + loadKg) * 100) / 100

    fun snapshot(): DeclarationAssignment {
        return DeclarationAssignment(
            code = code,
            title = title,
            windowDays = windowDays,
            category = category,
            ratio = ratio,
            revision = revision,
            version = version,
            odometer = odometer,
            depot = depot,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a declaration assignment in status idle. */
fun newDeclarationAssignment(code: String, title: String): DeclarationAssignment {
    return DeclarationAssignment(
        code = code,
        title = title,
        windowDays = 0,
        category = "",
        ratio = 0.0,
        revision = 0,
        version = 0,
        odometer = 0,
        depot = "",
        loadKg = 0.0,
        status = DeclarationAssignmentStatus.IDLE,
    )
}
