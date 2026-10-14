package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The parcel mapping records of lastmile's parcel work, and the rules, stores, and calculations around them.

/** Where a parcel mapping is in its life cycle. */
enum class ParcelMappingStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a parcel mapping and its current status. */
data class ParcelMapping(
    var code: String,
    var title: String,
    var version: Int,
    var rate: Double,
    var checksum: String,
    var attempts: Int,
    var tags: MutableList<String>,
    var routeCode: String,
    var status: ParcelMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelMappingStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelMappingStatus): Boolean {
        when (status) {
            ParcelMappingStatus.CREATED -> {
                when (next) {
                    ParcelMappingStatus.VALIDATED -> return true
                }
                return false
            }
            ParcelMappingStatus.VALIDATED -> {
                when (next) {
                    ParcelMappingStatus.PUBLISHED, ParcelMappingStatus.RETIRED -> return true
                }
                return false
            }
            ParcelMappingStatus.PUBLISHED -> {
                when (next) {
                    ParcelMappingStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelMappingStatus) {
        if (!canMoveTo(next)) {
            error("parcel mapping $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netTotal(): Double = round((attempts.toDouble() - rate) * 100) / 100

    fun snapshot(): ParcelMapping {
        return ParcelMapping(
            code = code,
            title = title,
            version = version,
            rate = rate,
            checksum = checksum,
            attempts = attempts,
            tags = tags,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a parcel mapping in status created. */
fun newParcelMapping(code: String, title: String): ParcelMapping {
    return ParcelMapping(
        code = code,
        title = title,
        version = 0,
        rate = 0.0,
        checksum = "",
        attempts = 0,
        tags = mutableListOf<String>(),
        routeCode = "",
        status = ParcelMappingStatus.CREATED,
    )
}
