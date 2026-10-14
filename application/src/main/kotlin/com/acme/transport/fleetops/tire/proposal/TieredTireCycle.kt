package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered tire cycle is in its life cycle. */
enum class TieredTireCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a tiered tire cycle and its current status. */
data class TieredTireCycle(
    var code: String,
    var title: String,
    var lane: String,
    var version: Int,
    var startsAt: Int,
    var endsAt: Int,
    var shiftCode: String,
    var externalRef: String,
    var visible: Boolean,
    var routeCode: String,
    var loadKg: Double,
    var status: TieredTireCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTireCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTireCycleStatus): Boolean {
        when (status) {
            TieredTireCycleStatus.PROPOSED -> return next == TieredTireCycleStatus.APPROVED || next == TieredTireCycleStatus.WITHDRAWN
            TieredTireCycleStatus.APPROVED -> {
                when (next) {
                    TieredTireCycleStatus.REJECTED, TieredTireCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
            TieredTireCycleStatus.REJECTED -> {
                when (next) {
                    TieredTireCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredTireCycleStatus) {
        if (!canMoveTo(next)) {
            error("tiered tire cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun netTotal(): Double = round((loadKg + version.toDouble()) * 100) / 100

    fun snapshot(): TieredTireCycle {
        return TieredTireCycle(
            code = code,
            title = title,
            lane = lane,
            version = version,
            startsAt = startsAt,
            endsAt = endsAt,
            shiftCode = shiftCode,
            externalRef = externalRef,
            visible = visible,
            routeCode = routeCode,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a tiered tire cycle in status proposed. */
fun newTieredTireCycle(code: String, title: String): TieredTireCycle {
    return TieredTireCycle(
        code = code,
        title = title,
        lane = "",
        version = 0,
        startsAt = 0,
        endsAt = 0,
        shiftCode = "",
        externalRef = "",
        visible = false,
        routeCode = "",
        loadKg = 0.0,
        status = TieredTireCycleStatus.PROPOSED,
    )
}
