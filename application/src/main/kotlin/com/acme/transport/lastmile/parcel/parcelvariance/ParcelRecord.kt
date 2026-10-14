package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel record is in its life cycle. */
enum class ParcelRecordStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One parcel record, with what the planners need to act on it. */
data class ParcelRecord(
    var code: String,
    var label: String,
    var threshold: Double,
    var startsAt: Int,
    var approved: Boolean,
    var attributes: MutableMap<String, String>,
    var vehicleId: String,
    var expiresAt: Int,
    var capacity: Int,
    var phone: String,
    var externalRef: String,
    var status: ParcelRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelRecordStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelRecordStatus): Boolean {
        when (status) {
            ParcelRecordStatus.DRAFT -> return next == ParcelRecordStatus.PENDING
            ParcelRecordStatus.PENDING -> return next == ParcelRecordStatus.SUSPENDED
            ParcelRecordStatus.ACTIVE -> return next == ParcelRecordStatus.SUSPENDED
            ParcelRecordStatus.SUSPENDED -> {
                when (next) {
                    ParcelRecordStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelRecordStatus) {
        if (!canMoveTo(next)) {
            error("parcel record $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"parcel record"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round(startsAt.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): ParcelRecord {
        return ParcelRecord(
            code = code,
            label = label,
            threshold = threshold,
            startsAt = startsAt,
            approved = approved,
            attributes = attributes,
            vehicleId = vehicleId,
            expiresAt = expiresAt,
            capacity = capacity,
            phone = phone,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a parcel record in status draft. */
fun newParcelRecord(code: String, label: String): ParcelRecord {
    return ParcelRecord(
        code = code,
        label = label,
        threshold = 0.0,
        startsAt = 0,
        approved = false,
        attributes = mutableMapOf<String, String>(),
        vehicleId = "",
        expiresAt = 0,
        capacity = 0,
        phone = "",
        externalRef = "",
        status = ParcelRecordStatus.DRAFT,
    )
}
