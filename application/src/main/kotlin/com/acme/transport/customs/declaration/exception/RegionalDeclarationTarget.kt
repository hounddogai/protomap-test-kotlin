package com.acme.transport.customs.declaration.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional declaration target is in its life cycle. */
enum class RegionalDeclarationTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A regional declaration target. Its code is unique within a tenant. */
data class RegionalDeclarationTarget(
    var code: String,
    var name: String,
    var startsAt: Int,
    var ratio: Double,
    var timezone: String,
    var label: String,
    var contactEmail: String,
    var ceiling: Double,
    var quantity: Int,
    var updatedAt: Int,
    var notes: String,
    var status: RegionalDeclarationTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDeclarationTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDeclarationTargetStatus): Boolean {
        when (status) {
            RegionalDeclarationTargetStatus.OPEN -> return next == RegionalDeclarationTargetStatus.RESOLVED
            RegionalDeclarationTargetStatus.IN_REVIEW -> {
                when (next) {
                    RegionalDeclarationTargetStatus.RESOLVED, RegionalDeclarationTargetStatus.REOPENED -> return true
                }
                return false
            }
            RegionalDeclarationTargetStatus.RESOLVED -> return next == RegionalDeclarationTargetStatus.REOPENED || next == RegionalDeclarationTargetStatus.ARCHIVED
            RegionalDeclarationTargetStatus.REOPENED -> {
                when (next) {
                    RegionalDeclarationTargetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDeclarationTargetStatus) {
        if (!canMoveTo(next)) {
            error("regional declaration target $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($name)"

    fun adjustedLoad(): Double = round((updatedAt.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): RegionalDeclarationTarget {
        return RegionalDeclarationTarget(
            code = code,
            name = name,
            startsAt = startsAt,
            ratio = ratio,
            timezone = timezone,
            label = label,
            contactEmail = contactEmail,
            ceiling = ceiling,
            quantity = quantity,
            updatedAt = updatedAt,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional declaration target in status open. */
fun newRegionalDeclarationTarget(code: String, name: String): RegionalDeclarationTarget {
    return RegionalDeclarationTarget(
        code = code,
        name = name,
        startsAt = 0,
        ratio = 0.0,
        timezone = "",
        label = "",
        contactEmail = "",
        ceiling = 0.0,
        quantity = 0,
        updatedAt = 0,
        notes = "",
        status = RegionalDeclarationTargetStatus.OPEN,
    )
}
