package com.acme.transport.customs.broker.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered broker manifest is in its life cycle. */
enum class TieredBrokerManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a tiered broker manifest and its current status. */
data class TieredBrokerManifest(
    var code: String,
    var title: String,
    var name: String,
    var cost: Double,
    var ownerId: String,
    var odometer: Int,
    var status: TieredBrokerManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredBrokerManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredBrokerManifestStatus): Boolean {
        when (status) {
            TieredBrokerManifestStatus.PROPOSED -> return next == TieredBrokerManifestStatus.APPROVED
            TieredBrokerManifestStatus.APPROVED -> return next == TieredBrokerManifestStatus.REJECTED || next == TieredBrokerManifestStatus.WITHDRAWN
            TieredBrokerManifestStatus.REJECTED -> {
                when (next) {
                    TieredBrokerManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredBrokerManifestStatus) {
        if (!canMoveTo(next)) {
            error("tiered broker manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun netValue(): Double = round(odometer.toDouble() * cost * 100) / 100

    fun snapshot(): TieredBrokerManifest {
        return TieredBrokerManifest(
            code = code,
            title = title,
            name = name,
            cost = cost,
            ownerId = ownerId,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered broker manifest in status proposed. */
fun newTieredBrokerManifest(code: String, title: String): TieredBrokerManifest {
    return TieredBrokerManifest(
        code = code,
        title = title,
        name = "",
        cost = 0.0,
        ownerId = "",
        odometer = 0,
        status = TieredBrokerManifestStatus.PROPOSED,
    )
}
