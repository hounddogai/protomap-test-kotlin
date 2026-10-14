package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone group is in its life cycle. */
enum class ZoneGroupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A zone group. Its code is unique within a tenant. */
data class ZoneGroup(
    var code: String,
    var title: String,
    var cost: Double,
    var discount: Double,
    var label: String,
    var durationMinutes: Int,
    var ceiling: Double,
    var status: ZoneGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneGroupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneGroupStatus): Boolean {
        when (status) {
            ZoneGroupStatus.DRAFT -> {
                when (next) {
                    ZoneGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            ZoneGroupStatus.PENDING -> {
                when (next) {
                    ZoneGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            ZoneGroupStatus.ACTIVE -> {
                when (next) {
                    ZoneGroupStatus.SUSPENDED -> return true
                }
                return false
            }
            ZoneGroupStatus.SUSPENDED -> {
                when (next) {
                    ZoneGroupStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneGroupStatus) {
        if (!canMoveTo(next)) {
            error("zone group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun adjustedVolume(): Double = round((ceiling + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): ZoneGroup {
        return ZoneGroup(
            code = code,
            title = title,
            cost = cost,
            discount = discount,
            label = label,
            durationMinutes = durationMinutes,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a zone group in status draft. */
fun newZoneGroup(code: String, title: String): ZoneGroup {
    return ZoneGroup(
        code = code,
        title = title,
        cost = 0.0,
        discount = 0.0,
        label = "",
        durationMinutes = 0,
        ceiling = 0.0,
        status = ZoneGroupStatus.DRAFT,
    )
}
