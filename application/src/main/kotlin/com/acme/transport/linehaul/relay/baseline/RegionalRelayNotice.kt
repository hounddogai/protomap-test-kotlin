package com.acme.transport.linehaul.relay.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional relay notice is in its life cycle. */
enum class RegionalRelayNoticeStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a regional relay notice and its current status. */
data class RegionalRelayNotice(
    var code: String,
    var name: String,
    var maximum: Double,
    var routeCode: String,
    var rate: Double,
    var status: RegionalRelayNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalRelayNoticeStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalRelayNoticeStatus): Boolean {
        when (status) {
            RegionalRelayNoticeStatus.IDLE -> {
                when (next) {
                    RegionalRelayNoticeStatus.STOPPED -> return true
                }
                return false
            }
            RegionalRelayNoticeStatus.WARMING -> {
                when (next) {
                    RegionalRelayNoticeStatus.DRAINING -> return true
                }
                return false
            }
            RegionalRelayNoticeStatus.READY -> return next == RegionalRelayNoticeStatus.DRAINING || next == RegionalRelayNoticeStatus.STOPPED
            RegionalRelayNoticeStatus.DRAINING -> {
                when (next) {
                    RegionalRelayNoticeStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalRelayNoticeStatus) {
        if (!canMoveTo(next)) {
            error("regional relay notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round((rate + maximum) * 100) / 100

    fun snapshot(): RegionalRelayNotice {
        return RegionalRelayNotice(
            code = code,
            name = name,
            maximum = maximum,
            routeCode = routeCode,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional relay notice in status idle. */
fun newRegionalRelayNotice(code: String, name: String): RegionalRelayNotice {
    return RegionalRelayNotice(
        code = code,
        name = name,
        maximum = 0.0,
        routeCode = "",
        rate = 0.0,
        status = RegionalRelayNoticeStatus.IDLE,
    )
}
