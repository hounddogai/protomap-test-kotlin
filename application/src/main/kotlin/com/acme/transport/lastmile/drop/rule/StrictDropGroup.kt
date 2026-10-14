package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict drop group is in its life cycle. */
enum class StrictDropGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict drop group as the drop team keeps it. */
data class StrictDropGroup(
    var code: String,
    var title: String,
    var quantity: Int,
    var description: String,
    var discount: Double,
    var dock: String,
    var status: StrictDropGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDropGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDropGroupStatus): Boolean {
        when (status) {
            StrictDropGroupStatus.DRAFT -> return next == StrictDropGroupStatus.CLOSED
            StrictDropGroupStatus.PENDING -> return next == StrictDropGroupStatus.SUSPENDED
            StrictDropGroupStatus.ACTIVE -> {
                when (next) {
                    StrictDropGroupStatus.SUSPENDED, StrictDropGroupStatus.CLOSED -> return true
                }
                return false
            }
            StrictDropGroupStatus.SUSPENDED -> {
                when (next) {
                    StrictDropGroupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDropGroupStatus) {
        if (!canMoveTo(next)) {
            error("strict drop group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveLoad(): Double = round(discount * quantity.toDouble() * 100) / 100

    fun snapshot(): StrictDropGroup {
        return StrictDropGroup(
            code = code,
            title = title,
            quantity = quantity,
            description = description,
            discount = discount,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict drop group in status draft. */
fun newStrictDropGroup(code: String, title: String): StrictDropGroup {
    return StrictDropGroup(
        code = code,
        title = title,
        quantity = 0,
        description = "",
        discount = 0.0,
        dock = "",
        status = StrictDropGroupStatus.DRAFT,
    )
}
