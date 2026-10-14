package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped broker manifest is in its life cycle. */
enum class CappedBrokerManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capped broker manifest as the broker team keeps it. */
data class CappedBrokerManifest(
    var code: String,
    var name: String,
    var category: String,
    var weight: Double,
    var driverId: String,
    var checksum: String,
    var ownerId: String,
    var limit: Int,
    var externalRef: String,
    var cost: Double,
    var pallets: Int,
    var status: CappedBrokerManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedBrokerManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedBrokerManifestStatus): Boolean {
        when (status) {
            CappedBrokerManifestStatus.OPEN -> {
                when (next) {
                    CappedBrokerManifestStatus.RESOLVED -> return true
                }
                return false
            }
            CappedBrokerManifestStatus.IN_REVIEW -> return next == CappedBrokerManifestStatus.ARCHIVED
            CappedBrokerManifestStatus.RESOLVED -> return next == CappedBrokerManifestStatus.REOPENED || next == CappedBrokerManifestStatus.ARCHIVED
            CappedBrokerManifestStatus.REOPENED -> return next == CappedBrokerManifestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CappedBrokerManifestStatus) {
        if (!canMoveTo(next)) {
            error("capped broker manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netVolume(): Double = round((limit.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): CappedBrokerManifest {
        return CappedBrokerManifest(
            code = code,
            name = name,
            category = category,
            weight = weight,
            driverId = driverId,
            checksum = checksum,
            ownerId = ownerId,
            limit = limit,
            externalRef = externalRef,
            cost = cost,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped broker manifest in status open. */
fun newCappedBrokerManifest(code: String, name: String): CappedBrokerManifest {
    return CappedBrokerManifest(
        code = code,
        name = name,
        category = "",
        weight = 0.0,
        driverId = "",
        checksum = "",
        ownerId = "",
        limit = 0,
        externalRef = "",
        cost = 0.0,
        pallets = 0,
        status = CappedBrokerManifestStatus.OPEN,
    )
}
