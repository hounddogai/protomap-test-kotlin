package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary route offset digest is in its life cycle. */
enum class PrimaryRouteOffsetDigestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary route offset digest. Its code is unique within a tenant. */
data class PrimaryRouteOffsetDigest(
    var code: String,
    var name: String,
    var pallets: Int,
    var attempts: Int,
    var odometer: Int,
    var checksum: String,
    var status: PrimaryRouteOffsetDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRouteOffsetDigestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRouteOffsetDigestStatus): Boolean {
        when (status) {
            PrimaryRouteOffsetDigestStatus.IDLE -> return next == PrimaryRouteOffsetDigestStatus.STOPPED
            PrimaryRouteOffsetDigestStatus.WARMING -> {
                when (next) {
                    PrimaryRouteOffsetDigestStatus.READY, PrimaryRouteOffsetDigestStatus.DRAINING -> return true
                }
                return false
            }
            PrimaryRouteOffsetDigestStatus.READY -> return next == PrimaryRouteOffsetDigestStatus.DRAINING
            PrimaryRouteOffsetDigestStatus.DRAINING -> return next == PrimaryRouteOffsetDigestStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PrimaryRouteOffsetDigestStatus) {
        if (!canMoveTo(next)) {
            error("primary route offset digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedLoad(): Double = round(attempts.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): PrimaryRouteOffsetDigest {
        return PrimaryRouteOffsetDigest(
            code = code,
            name = name,
            pallets = pallets,
            attempts = attempts,
            odometer = odometer,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary route offset digest in status idle. */
fun newPrimaryRouteOffsetDigest(code: String, name: String): PrimaryRouteOffsetDigest {
    return PrimaryRouteOffsetDigest(
        code = code,
        name = name,
        pallets = 0,
        attempts = 0,
        odometer = 0,
        checksum = "",
        status = PrimaryRouteOffsetDigestStatus.IDLE,
    )
}
