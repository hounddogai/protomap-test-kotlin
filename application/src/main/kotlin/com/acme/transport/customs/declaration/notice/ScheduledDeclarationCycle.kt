package com.acme.transport.customs.declaration.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled declaration cycle is in its life cycle. */
enum class ScheduledDeclarationCycleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled declaration cycle as the declaration team keeps it. */
data class ScheduledDeclarationCycle(
    var code: String,
    var title: String,
    var depot: String,
    var ownerId: String,
    var distanceKm: Double,
    var rate: Double,
    var status: ScheduledDeclarationCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDeclarationCycleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDeclarationCycleStatus): Boolean {
        when (status) {
            ScheduledDeclarationCycleStatus.DRAFT -> return next == ScheduledDeclarationCycleStatus.PENDING || next == ScheduledDeclarationCycleStatus.CLOSED
            ScheduledDeclarationCycleStatus.PENDING -> return next == ScheduledDeclarationCycleStatus.SUSPENDED || next == ScheduledDeclarationCycleStatus.CLOSED
            ScheduledDeclarationCycleStatus.ACTIVE -> return next == ScheduledDeclarationCycleStatus.CLOSED
            ScheduledDeclarationCycleStatus.SUSPENDED -> {
                when (next) {
                    ScheduledDeclarationCycleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledDeclarationCycleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled declaration cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedLoad(): Double = round(rate * distanceKm * 100) / 100

    fun snapshot(): ScheduledDeclarationCycle {
        return ScheduledDeclarationCycle(
            code = code,
            title = title,
            depot = depot,
            ownerId = ownerId,
            distanceKm = distanceKm,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled declaration cycle in status draft. */
fun newScheduledDeclarationCycle(code: String, title: String): ScheduledDeclarationCycle {
    return ScheduledDeclarationCycle(
        code = code,
        title = title,
        depot = "",
        ownerId = "",
        distanceKm = 0.0,
        rate = 0.0,
        status = ScheduledDeclarationCycleStatus.DRAFT,
    )
}
