package com.acme.transport.customs.broker.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped broker mapping is in its life cycle. */
enum class CappedBrokerMappingStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped broker mapping. Its code is unique within a tenant. */
data class CappedBrokerMapping(
    var code: String,
    var label: String,
    var driverId: String,
    var ownerId: String,
    var distanceKm: Double,
    var carrier: String,
    var contactEmail: String,
    var approved: Boolean,
    var weight: Double,
    var status: CappedBrokerMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedBrokerMappingStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedBrokerMappingStatus): Boolean {
        when (status) {
            CappedBrokerMappingStatus.PROPOSED -> {
                when (next) {
                    CappedBrokerMappingStatus.APPROVED, CappedBrokerMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedBrokerMappingStatus.APPROVED -> {
                when (next) {
                    CappedBrokerMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedBrokerMappingStatus.REJECTED -> {
                when (next) {
                    CappedBrokerMappingStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedBrokerMappingStatus) {
        if (!canMoveTo(next)) {
            error("capped broker mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netExposure(): Double = round((weight + distanceKm) * 100) / 100

    fun snapshot(): CappedBrokerMapping {
        return CappedBrokerMapping(
            code = code,
            label = label,
            driverId = driverId,
            ownerId = ownerId,
            distanceKm = distanceKm,
            carrier = carrier,
            contactEmail = contactEmail,
            approved = approved,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped broker mapping in status proposed. */
fun newCappedBrokerMapping(code: String, label: String): CappedBrokerMapping {
    return CappedBrokerMapping(
        code = code,
        label = label,
        driverId = "",
        ownerId = "",
        distanceKm = 0.0,
        carrier = "",
        contactEmail = "",
        approved = false,
        weight = 0.0,
        status = CappedBrokerMappingStatus.PROPOSED,
    )
}
