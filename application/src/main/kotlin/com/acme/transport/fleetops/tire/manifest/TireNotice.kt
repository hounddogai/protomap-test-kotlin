package com.acme.transport.fleetops.tire.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire notice is in its life cycle. */
enum class TireNoticeStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A tire notice as the tire team keeps it. */
data class TireNotice(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var pallets: Int,
    var segment: String,
    var ratio: Double,
    var amount: Double,
    var status: TireNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireNoticeStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireNoticeStatus): Boolean {
        when (status) {
            TireNoticeStatus.REQUESTED -> {
                when (next) {
                    TireNoticeStatus.GRANTED, TireNoticeStatus.EXPIRED -> return true
                }
                return false
            }
            TireNoticeStatus.GRANTED -> return next == TireNoticeStatus.EXPIRED
            TireNoticeStatus.REVOKED -> return next == TireNoticeStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TireNoticeStatus) {
        if (!canMoveTo(next)) {
            error("tire notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tire notice"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedLoad(): Double = round((ratio - amount) * 100) / 100

    fun snapshot(): TireNotice {
        return TireNotice(
            code = code,
            name = name,
            tags = tags,
            pallets = pallets,
            segment = segment,
            ratio = ratio,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tire notice in status requested. */
fun newTireNotice(code: String, name: String): TireNotice {
    return TireNotice(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        pallets = 0,
        segment = "",
        ratio = 0.0,
        amount = 0.0,
        status = TireNoticeStatus.REQUESTED,
    )
}
