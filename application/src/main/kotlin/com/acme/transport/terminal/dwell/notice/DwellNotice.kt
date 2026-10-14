package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dwell notice records of terminal's dwell work, and the rules, stores, and calculations around them.

/** Where a dwell notice is in its life cycle. */
enum class DwellNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A dwell notice. Its code is unique within a tenant. */
data class DwellNotice(
    var code: String,
    var title: String,
    var notes: String,
    var limit: Int,
    var quantity: Int,
    var fuelLiters: Double,
    var checksum: String,
    var tags: MutableList<String>,
    var phone: String,
    var depot: String,
    var pallets: Int,
    var status: DwellNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellNoticeStatus): Boolean {
        when (status) {
            DwellNoticeStatus.PROPOSED -> {
                when (next) {
                    DwellNoticeStatus.REJECTED -> return true
                }
                return false
            }
            DwellNoticeStatus.APPROVED -> return next == DwellNoticeStatus.REJECTED
            DwellNoticeStatus.REJECTED -> return next == DwellNoticeStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DwellNoticeStatus) {
        if (!canMoveTo(next)) {
            error("dwell notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"dwell notice"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round((fuelLiters + limit.toDouble()) * 100) / 100

    fun snapshot(): DwellNotice {
        return DwellNotice(
            code = code,
            title = title,
            notes = notes,
            limit = limit,
            quantity = quantity,
            fuelLiters = fuelLiters,
            checksum = checksum,
            tags = tags,
            phone = phone,
            depot = depot,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a dwell notice in status proposed. */
fun newDwellNotice(code: String, title: String): DwellNotice {
    return DwellNotice(
        code = code,
        title = title,
        notes = "",
        limit = 0,
        quantity = 0,
        fuelLiters = 0.0,
        checksum = "",
        tags = mutableListOf<String>(),
        phone = "",
        depot = "",
        pallets = 0,
        status = DwellNoticeStatus.PROPOSED,
    )
}
