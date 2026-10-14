package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The zone mapping records of network's zone work, and the rules, stores, and calculations around them.

/** Where a zone mapping is in its life cycle. */
enum class ZoneMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A zone mapping. Its code is unique within a tenant. */
data class ZoneMapping(
    var code: String,
    var name: String,
    var discount: Double,
    var pallets: Int,
    var segment: String,
    var contactEmail: String,
    var margin: Double,
    var sequence: Int,
    var region: String,
    var status: ZoneMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneMappingStatus): Boolean {
        when (status) {
            ZoneMappingStatus.CREATED -> return next == ZoneMappingStatus.VALIDATED || next == ZoneMappingStatus.RETIRED
            ZoneMappingStatus.VALIDATED -> {
                when (next) {
                    ZoneMappingStatus.PUBLISHED, ZoneMappingStatus.RETIRED -> return true
                }
                return false
            }
            ZoneMappingStatus.PUBLISHED -> return next == ZoneMappingStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ZoneMappingStatus) {
        if (!canMoveTo(next)) {
            error("zone mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedValue(): Double = round((margin - pallets.toDouble()) * 100) / 100

    fun snapshot(): ZoneMapping {
        return ZoneMapping(
            code = code,
            name = name,
            discount = discount,
            pallets = pallets,
            segment = segment,
            contactEmail = contactEmail,
            margin = margin,
            sequence = sequence,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a zone mapping in status created. */
fun newZoneMapping(code: String, name: String): ZoneMapping {
    return ZoneMapping(
        code = code,
        name = name,
        discount = 0.0,
        pallets = 0,
        segment = "",
        contactEmail = "",
        margin = 0.0,
        sequence = 0,
        region = "",
        status = ZoneMappingStatus.CREATED,
    )
}
