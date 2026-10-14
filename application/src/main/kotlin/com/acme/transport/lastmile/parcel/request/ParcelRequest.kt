package com.acme.transport.lastmile.parcel.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The parcel request records of lastmile's parcel work, and the rules, stores, and calculations around them.

/** Where a parcel request is in its life cycle. */
enum class ParcelRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a parcel request and its current status. */
data class ParcelRequest(
    var code: String,
    var label: String,
    var stopCount: Int,
    var floorValue: Double,
    var version: Int,
    var attributes: MutableMap<String, String>,
    var checksum: String,
    var ratio: Double,
    var status: ParcelRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelRequestStatus): Boolean {
        when (status) {
            ParcelRequestStatus.REQUESTED -> return next == ParcelRequestStatus.GRANTED || next == ParcelRequestStatus.REVOKED
            ParcelRequestStatus.GRANTED -> return next == ParcelRequestStatus.REVOKED
            ParcelRequestStatus.REVOKED -> {
                when (next) {
                    ParcelRequestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelRequestStatus) {
        if (!canMoveTo(next)) {
            error("parcel request $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round(stopCount.toDouble() * version.toDouble() * 100) / 100

    fun snapshot(): ParcelRequest {
        return ParcelRequest(
            code = code,
            label = label,
            stopCount = stopCount,
            floorValue = floorValue,
            version = version,
            attributes = attributes,
            checksum = checksum,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a parcel request in status requested. */
fun newParcelRequest(code: String, label: String): ParcelRequest {
    return ParcelRequest(
        code = code,
        label = label,
        stopCount = 0,
        floorValue = 0.0,
        version = 0,
        attributes = mutableMapOf<String, String>(),
        checksum = "",
        ratio = 0.0,
        status = ParcelRequestStatus.REQUESTED,
    )
}
