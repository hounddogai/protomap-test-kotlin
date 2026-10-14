package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped service area mapping is in its life cycle. */
enum class CappedServiceAreaMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One capped service area mapping, with what the planners need to act on it. */
data class CappedServiceAreaMapping(
    var code: String,
    var label: String,
    var loadKg: Double,
    var contactEmail: String,
    var description: String,
    var pallets: Int,
    var status: CappedServiceAreaMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedServiceAreaMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedServiceAreaMappingStatus): Boolean {
        when (status) {
            CappedServiceAreaMappingStatus.REQUESTED -> {
                when (next) {
                    CappedServiceAreaMappingStatus.REVOKED -> return true
                }
                return false
            }
            CappedServiceAreaMappingStatus.GRANTED -> {
                when (next) {
                    CappedServiceAreaMappingStatus.REVOKED -> return true
                }
                return false
            }
            CappedServiceAreaMappingStatus.REVOKED -> {
                when (next) {
                    CappedServiceAreaMappingStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedServiceAreaMappingStatus) {
        if (!canMoveTo(next)) {
            error("capped service area mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"capped service area mapping"} $code"

    fun netExposure(): Double = round(loadKg * pallets.toDouble() * 100) / 100

    fun snapshot(): CappedServiceAreaMapping {
        return CappedServiceAreaMapping(
            code = code,
            label = label,
            loadKg = loadKg,
            contactEmail = contactEmail,
            description = description,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped service area mapping in status requested. */
fun newCappedServiceAreaMapping(code: String, label: String): CappedServiceAreaMapping {
    return CappedServiceAreaMapping(
        code = code,
        label = label,
        loadKg = 0.0,
        contactEmail = "",
        description = "",
        pallets = 0,
        status = CappedServiceAreaMappingStatus.REQUESTED,
    )
}
