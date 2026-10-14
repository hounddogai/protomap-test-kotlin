package com.acme.transport.network.zone.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone entry records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone entry is in its life cycle. */
enum class ZoneEntryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A zone entry. Its code is unique within a tenant. */
data class ZoneEntry(
    var code: String,
    var title: String,
    var phone: String,
    var region: String,
    var archived: Boolean,
    var expiresAt: Int,
    var weight: Double,
    var dock: String,
    var status: ZoneEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneEntryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneEntryStatus): Boolean {
        when (status) {
            ZoneEntryStatus.NEW -> {
                when (next) {
                    ZoneEntryStatus.DONE, ZoneEntryStatus.FAILED -> return true
                }
                return false
            }
            ZoneEntryStatus.QUEUED -> {
                when (next) {
                    ZoneEntryStatus.RUNNING -> return true
                }
                return false
            }
            ZoneEntryStatus.RUNNING -> return next == ZoneEntryStatus.DONE
            ZoneEntryStatus.DONE -> return next == ZoneEntryStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ZoneEntryStatus) {
        if (!canMoveTo(next)) {
            error("zone entry $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"zone entry"} $code"

    fun effectiveValue(): Double = round((expiresAt.toDouble() + weight) * 100) / 100

    fun snapshot(): ZoneEntry {
        return ZoneEntry(
            code = code,
            title = title,
            phone = phone,
            region = region,
            archived = archived,
            expiresAt = expiresAt,
            weight = weight,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a zone entry in status new. */
fun newZoneEntry(code: String, title: String): ZoneEntry {
    return ZoneEntry(
        code = code,
        title = title,
        phone = "",
        region = "",
        archived = false,
        expiresAt = 0,
        weight = 0.0,
        dock = "",
        status = ZoneEntryStatus.NEW,
    )
}
