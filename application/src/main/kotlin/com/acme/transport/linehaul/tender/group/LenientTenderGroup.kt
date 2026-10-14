package com.acme.transport.linehaul.tender.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient tender group records of linehaul's tender work, and the rules, stores, and calculations around
// them.

/** Where a lenient tender group is in its life cycle. */
enum class LenientTenderGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient tender group. Its code is unique within a tenant. */
data class LenientTenderGroup(
    var code: String,
    var title: String,
    var country: String,
    var quantity: Int,
    var ownerId: String,
    var maximum: Double,
    var routeCode: String,
    var name: String,
    var reviewer: String,
    var tags: MutableList<String>,
    var status: LenientTenderGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientTenderGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientTenderGroupStatus): Boolean {
        when (status) {
            LenientTenderGroupStatus.DRAFT -> return next == LenientTenderGroupStatus.CLOSED
            LenientTenderGroupStatus.PENDING -> return next == LenientTenderGroupStatus.ACTIVE || next == LenientTenderGroupStatus.CLOSED
            LenientTenderGroupStatus.ACTIVE -> {
                when (next) {
                    LenientTenderGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientTenderGroupStatus.SUSPENDED -> return next == LenientTenderGroupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientTenderGroupStatus) {
        if (!canMoveTo(next)) {
            error("lenient tender group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedLoad(): Double = round((quantity.toDouble() - maximum) * 100) / 100

    fun snapshot(): LenientTenderGroup {
        return LenientTenderGroup(
            code = code,
            title = title,
            country = country,
            quantity = quantity,
            ownerId = ownerId,
            maximum = maximum,
            routeCode = routeCode,
            name = name,
            reviewer = reviewer,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a lenient tender group in status draft. */
fun newLenientTenderGroup(code: String, title: String): LenientTenderGroup {
    return LenientTenderGroup(
        code = code,
        title = title,
        country = "",
        quantity = 0,
        ownerId = "",
        maximum = 0.0,
        routeCode = "",
        name = "",
        reviewer = "",
        tags = mutableListOf<String>(),
        status = LenientTenderGroupStatus.DRAFT,
    )
}
