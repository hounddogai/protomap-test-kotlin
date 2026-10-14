package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual ev charge tier is in its life cycle. */
enum class ManualEvChargeTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A manual ev charge tier as the ev charge team keeps it. */
data class ManualEvChargeTier(
    var code: String,
    var title: String,
    var region: String,
    var cost: Double,
    var attempts: Int,
    var status: ManualEvChargeTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualEvChargeTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualEvChargeTierStatus): Boolean {
        when (status) {
            ManualEvChargeTierStatus.IDLE -> return next == ManualEvChargeTierStatus.DRAINING || next == ManualEvChargeTierStatus.STOPPED
            ManualEvChargeTierStatus.WARMING -> return next == ManualEvChargeTierStatus.READY || next == ManualEvChargeTierStatus.DRAINING
            ManualEvChargeTierStatus.READY -> return next == ManualEvChargeTierStatus.DRAINING || next == ManualEvChargeTierStatus.STOPPED
            ManualEvChargeTierStatus.DRAINING -> {
                when (next) {
                    ManualEvChargeTierStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualEvChargeTierStatus) {
        if (!canMoveTo(next)) {
            error("manual ev charge tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedLoad(): Double = round((cost - attempts.toDouble()) * 100) / 100

    fun snapshot(): ManualEvChargeTier {
        return ManualEvChargeTier(
            code = code,
            title = title,
            region = region,
            cost = cost,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual ev charge tier in status idle. */
fun newManualEvChargeTier(code: String, title: String): ManualEvChargeTier {
    return ManualEvChargeTier(
        code = code,
        title = title,
        region = "",
        cost = 0.0,
        attempts = 0,
        status = ManualEvChargeTierStatus.IDLE,
    )
}
