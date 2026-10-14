package com.acme.transport.customs.declaration.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a declaration slot is in its life cycle. */
enum class DeclarationSlotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A declaration slot as the declaration team keeps it. */
data class DeclarationSlot(
    var code: String,
    var title: String,
    var name: String,
    var region: String,
    var ratio: Double,
    var quantity: Int,
    var dock: String,
    var capacity: Int,
    var timezone: String,
    var score: Double,
    var maximum: Double,
    var status: DeclarationSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DeclarationSlotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DeclarationSlotStatus): Boolean {
        when (status) {
            DeclarationSlotStatus.PROPOSED -> return next == DeclarationSlotStatus.APPROVED
            DeclarationSlotStatus.APPROVED -> {
                when (next) {
                    DeclarationSlotStatus.REJECTED, DeclarationSlotStatus.WITHDRAWN -> return true
                }
                return false
            }
            DeclarationSlotStatus.REJECTED -> return next == DeclarationSlotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DeclarationSlotStatus) {
        if (!canMoveTo(next)) {
            error("declaration slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun netExposure(): Double = round((capacity.toDouble() + score) * 100) / 100

    fun snapshot(): DeclarationSlot {
        return DeclarationSlot(
            code = code,
            title = title,
            name = name,
            region = region,
            ratio = ratio,
            quantity = quantity,
            dock = dock,
            capacity = capacity,
            timezone = timezone,
            score = score,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a declaration slot in status proposed. */
fun newDeclarationSlot(code: String, title: String): DeclarationSlot {
    return DeclarationSlot(
        code = code,
        title = title,
        name = "",
        region = "",
        ratio = 0.0,
        quantity = 0,
        dock = "",
        capacity = 0,
        timezone = "",
        score = 0.0,
        maximum = 0.0,
        status = DeclarationSlotStatus.PROPOSED,
    )
}
