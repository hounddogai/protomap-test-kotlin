package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped tender window is in its life cycle. */
enum class CappedTenderWindowStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped tender window. Its code is unique within a tenant. */
data class CappedTenderWindow(
    var code: String,
    var name: String,
    var title: String,
    var capacity: Int,
    var ceiling: Double,
    var quantity: Int,
    var phone: String,
    var status: CappedTenderWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTenderWindowStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTenderWindowStatus): Boolean {
        when (status) {
            CappedTenderWindowStatus.CREATED -> return next == CappedTenderWindowStatus.PUBLISHED
            CappedTenderWindowStatus.VALIDATED -> return next == CappedTenderWindowStatus.PUBLISHED
            CappedTenderWindowStatus.PUBLISHED -> {
                when (next) {
                    CappedTenderWindowStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedTenderWindowStatus) {
        if (!canMoveTo(next)) {
            error("capped tender window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round((capacity.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): CappedTenderWindow {
        return CappedTenderWindow(
            code = code,
            name = name,
            title = title,
            capacity = capacity,
            ceiling = ceiling,
            quantity = quantity,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped tender window in status created. */
fun newCappedTenderWindow(code: String, name: String): CappedTenderWindow {
    return CappedTenderWindow(
        code = code,
        name = name,
        title = "",
        capacity = 0,
        ceiling = 0.0,
        quantity = 0,
        phone = "",
        status = CappedTenderWindowStatus.CREATED,
    )
}
