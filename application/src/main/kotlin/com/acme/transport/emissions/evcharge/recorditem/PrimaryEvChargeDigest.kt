package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary ev charge digest is in its life cycle. */
enum class PrimaryEvChargeDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary ev charge digest as the ev charge team keeps it. */
data class PrimaryEvChargeDigest(
    var code: String,
    var name: String,
    var ceiling: Double,
    var quantity: Int,
    var region: String,
    var version: Int,
    var visible: Boolean,
    var revision: Int,
    var unit: String,
    var status: PrimaryEvChargeDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryEvChargeDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryEvChargeDigestStatus): Boolean {
        when (status) {
            PrimaryEvChargeDigestStatus.IDLE -> return next == PrimaryEvChargeDigestStatus.WARMING || next == PrimaryEvChargeDigestStatus.STOPPED
            PrimaryEvChargeDigestStatus.WARMING -> {
                when (next) {
                    PrimaryEvChargeDigestStatus.DRAINING, PrimaryEvChargeDigestStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryEvChargeDigestStatus.READY -> return next == PrimaryEvChargeDigestStatus.DRAINING
            PrimaryEvChargeDigestStatus.DRAINING -> {
                when (next) {
                    PrimaryEvChargeDigestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryEvChargeDigestStatus) {
        if (!canMoveTo(next)) {
            error("primary ev charge digest $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun weightedLoad(): Double = round((ceiling + version.toDouble()) * 100) / 100

    fun snapshot(): PrimaryEvChargeDigest {
        return PrimaryEvChargeDigest(
            code = code,
            name = name,
            ceiling = ceiling,
            quantity = quantity,
            region = region,
            version = version,
            visible = visible,
            revision = revision,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a primary ev charge digest in status idle. */
fun newPrimaryEvChargeDigest(code: String, name: String): PrimaryEvChargeDigest {
    return PrimaryEvChargeDigest(
        code = code,
        name = name,
        ceiling = 0.0,
        quantity = 0,
        region = "",
        version = 0,
        visible = false,
        revision = 0,
        unit = "",
        status = PrimaryEvChargeDigestStatus.IDLE,
    )
}
