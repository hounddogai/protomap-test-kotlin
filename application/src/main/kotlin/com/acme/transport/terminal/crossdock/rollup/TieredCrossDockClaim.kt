package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered cross dock claim is in its life cycle. */
enum class TieredCrossDockClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a tiered cross dock claim and its current status. */
data class TieredCrossDockClaim(
    var code: String,
    var name: String,
    var summary: String,
    var quantity: Int,
    var odometer: Int,
    var pinned: Boolean,
    var revision: Int,
    var driverId: String,
    var category: String,
    var score: Double,
    var status: TieredCrossDockClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCrossDockClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCrossDockClaimStatus): Boolean {
        when (status) {
            TieredCrossDockClaimStatus.CREATED -> {
                when (next) {
                    TieredCrossDockClaimStatus.PUBLISHED, TieredCrossDockClaimStatus.RETIRED -> return true
                }
                return false
            }
            TieredCrossDockClaimStatus.VALIDATED -> return next == TieredCrossDockClaimStatus.PUBLISHED || next == TieredCrossDockClaimStatus.RETIRED
            TieredCrossDockClaimStatus.PUBLISHED -> {
                when (next) {
                    TieredCrossDockClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredCrossDockClaimStatus) {
        if (!canMoveTo(next)) {
            error("tiered cross dock claim $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round(revision.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): TieredCrossDockClaim {
        return TieredCrossDockClaim(
            code = code,
            name = name,
            summary = summary,
            quantity = quantity,
            odometer = odometer,
            pinned = pinned,
            revision = revision,
            driverId = driverId,
            category = category,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a tiered cross dock claim in status created. */
fun newTieredCrossDockClaim(code: String, name: String): TieredCrossDockClaim {
    return TieredCrossDockClaim(
        code = code,
        name = name,
        summary = "",
        quantity = 0,
        odometer = 0,
        pinned = false,
        revision = 0,
        driverId = "",
        category = "",
        score = 0.0,
        status = TieredCrossDockClaimStatus.CREATED,
    )
}
