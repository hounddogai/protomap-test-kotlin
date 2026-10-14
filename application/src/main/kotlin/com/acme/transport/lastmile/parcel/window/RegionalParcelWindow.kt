package com.acme.transport.lastmile.parcel.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional parcel window records of lastmile's parcel work, and the rules, stores, and calculations
// around them.

/** Where a regional parcel window is in its life cycle. */
enum class RegionalParcelWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A regional parcel window. Its code is unique within a tenant. */
data class RegionalParcelWindow(
    var code: String,
    var title: String,
    var source: String,
    var tags: MutableList<String>,
    var quantity: Int,
    var retries: Int,
    var durationMinutes: Int,
    var status: RegionalParcelWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalParcelWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalParcelWindowStatus): Boolean {
        when (status) {
            RegionalParcelWindowStatus.IDLE -> return next == RegionalParcelWindowStatus.READY
            RegionalParcelWindowStatus.WARMING -> return next == RegionalParcelWindowStatus.READY || next == RegionalParcelWindowStatus.STOPPED
            RegionalParcelWindowStatus.READY -> {
                when (next) {
                    RegionalParcelWindowStatus.DRAINING, RegionalParcelWindowStatus.STOPPED -> return true
                }
                return false
            }
            RegionalParcelWindowStatus.DRAINING -> return next == RegionalParcelWindowStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RegionalParcelWindowStatus) {
        if (!canMoveTo(next)) {
            error("regional parcel window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round((quantity.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): RegionalParcelWindow {
        return RegionalParcelWindow(
            code = code,
            title = title,
            source = source,
            tags = tags,
            quantity = quantity,
            retries = retries,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional parcel window in status idle. */
fun newRegionalParcelWindow(code: String, title: String): RegionalParcelWindow {
    return RegionalParcelWindow(
        code = code,
        title = title,
        source = "",
        tags = mutableListOf<String>(),
        quantity = 0,
        retries = 0,
        durationMinutes = 0,
        status = RegionalParcelWindowStatus.IDLE,
    )
}
