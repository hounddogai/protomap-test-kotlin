package com.acme.transport.freightpay.accessorial.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default accessorial rollup is in its life cycle. */
enum class DefaultAccessorialRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A default accessorial rollup. Its code is unique within a tenant. */
data class DefaultAccessorialRollup(
    var code: String,
    var name: String,
    var odometer: Int,
    var channel: String,
    var checksum: String,
    var label: String,
    var capacity: Int,
    var retries: Int,
    var routeCode: String,
    var status: DefaultAccessorialRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultAccessorialRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultAccessorialRollupStatus): Boolean {
        when (status) {
            DefaultAccessorialRollupStatus.REQUESTED -> return next == DefaultAccessorialRollupStatus.GRANTED || next == DefaultAccessorialRollupStatus.REVOKED
            DefaultAccessorialRollupStatus.GRANTED -> {
                when (next) {
                    DefaultAccessorialRollupStatus.REVOKED, DefaultAccessorialRollupStatus.EXPIRED -> return true
                }
                return false
            }
            DefaultAccessorialRollupStatus.REVOKED -> return next == DefaultAccessorialRollupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: DefaultAccessorialRollupStatus) {
        if (!canMoveTo(next)) {
            error("default accessorial rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default accessorial rollup"} $code"

    fun projectedValue(): Double = round((retries.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): DefaultAccessorialRollup {
        return DefaultAccessorialRollup(
            code = code,
            name = name,
            odometer = odometer,
            channel = channel,
            checksum = checksum,
            label = label,
            capacity = capacity,
            retries = retries,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a default accessorial rollup in status requested. */
fun newDefaultAccessorialRollup(code: String, name: String): DefaultAccessorialRollup {
    return DefaultAccessorialRollup(
        code = code,
        name = name,
        odometer = 0,
        channel = "",
        checksum = "",
        label = "",
        capacity = 0,
        retries = 0,
        routeCode = "",
        status = DefaultAccessorialRollupStatus.REQUESTED,
    )
}
