package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered cross dock profile is in its life cycle. */
enum class TieredCrossDockProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tiered cross dock profile and its current status. */
data class TieredCrossDockProfile(
    var code: String,
    var title: String,
    var quantity: Int,
    var archived: Boolean,
    var floorValue: Double,
    var driverId: String,
    var channel: String,
    var sequence: Int,
    var dock: String,
    var status: TieredCrossDockProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCrossDockProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCrossDockProfileStatus): Boolean {
        when (status) {
            TieredCrossDockProfileStatus.REQUESTED -> {
                when (next) {
                    TieredCrossDockProfileStatus.GRANTED, TieredCrossDockProfileStatus.REVOKED -> return true
                }
                return false
            }
            TieredCrossDockProfileStatus.GRANTED -> return next == TieredCrossDockProfileStatus.REVOKED || next == TieredCrossDockProfileStatus.EXPIRED
            TieredCrossDockProfileStatus.REVOKED -> {
                when (next) {
                    TieredCrossDockProfileStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredCrossDockProfileStatus) {
        if (!canMoveTo(next)) {
            error("tiered cross dock profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedVolume(): Double = round((quantity.toDouble() + floorValue) * 100) / 100

    fun snapshot(): TieredCrossDockProfile {
        return TieredCrossDockProfile(
            code = code,
            title = title,
            quantity = quantity,
            archived = archived,
            floorValue = floorValue,
            driverId = driverId,
            channel = channel,
            sequence = sequence,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a tiered cross dock profile in status requested. */
fun newTieredCrossDockProfile(code: String, title: String): TieredCrossDockProfile {
    return TieredCrossDockProfile(
        code = code,
        title = title,
        quantity = 0,
        archived = false,
        floorValue = 0.0,
        driverId = "",
        channel = "",
        sequence = 0,
        dock = "",
        status = TieredCrossDockProfileStatus.REQUESTED,
    )
}
