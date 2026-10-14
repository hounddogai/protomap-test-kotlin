package com.acme.transport.network.node.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal node bundle is in its life cycle. */
enum class SeasonalNodeBundleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A seasonal node bundle as the node team keeps it. */
data class SeasonalNodeBundle(
    var code: String,
    var title: String,
    var pallets: Int,
    var weight: Double,
    var revision: Int,
    var ceiling: Double,
    var quantity: Int,
    var name: String,
    var floorValue: Double,
    var carrier: String,
    var status: SeasonalNodeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalNodeBundleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalNodeBundleStatus): Boolean {
        when (status) {
            SeasonalNodeBundleStatus.PROPOSED -> return next == SeasonalNodeBundleStatus.APPROVED
            SeasonalNodeBundleStatus.APPROVED -> {
                when (next) {
                    SeasonalNodeBundleStatus.REJECTED -> return true
                }
                return false
            }
            SeasonalNodeBundleStatus.REJECTED -> {
                when (next) {
                    SeasonalNodeBundleStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalNodeBundleStatus) {
        if (!canMoveTo(next)) {
            error("seasonal node bundle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $title"

    fun netExposure(): Double = round(pallets.toDouble() * floorValue * 100) / 100

    fun snapshot(): SeasonalNodeBundle {
        return SeasonalNodeBundle(
            code = code,
            title = title,
            pallets = pallets,
            weight = weight,
            revision = revision,
            ceiling = ceiling,
            quantity = quantity,
            name = name,
            floorValue = floorValue,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a seasonal node bundle in status proposed. */
fun newSeasonalNodeBundle(code: String, title: String): SeasonalNodeBundle {
    return SeasonalNodeBundle(
        code = code,
        title = title,
        pallets = 0,
        weight = 0.0,
        revision = 0,
        ceiling = 0.0,
        quantity = 0,
        name = "",
        floorValue = 0.0,
        carrier = "",
        status = SeasonalNodeBundleStatus.PROPOSED,
    )
}
