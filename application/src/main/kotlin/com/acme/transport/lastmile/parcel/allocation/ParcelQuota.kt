package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel quota is in its life cycle. */
enum class ParcelQuotaStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A parcel quota as the parcel team keeps it. */
data class ParcelQuota(
    var code: String,
    var title: String,
    var version: Int,
    var priority: Int,
    var reviewer: String,
    var odometer: Int,
    var weight: Double,
    var ceiling: Double,
    var margin: Double,
    var contactEmail: String,
    var limit: Int,
    var status: ParcelQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelQuotaStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelQuotaStatus): Boolean {
        when (status) {
            ParcelQuotaStatus.DRAFT -> {
                when (next) {
                    ParcelQuotaStatus.PENDING -> return true
                }
                return false
            }
            ParcelQuotaStatus.PENDING -> {
                when (next) {
                    ParcelQuotaStatus.SUSPENDED -> return true
                }
                return false
            }
            ParcelQuotaStatus.ACTIVE -> {
                when (next) {
                    ParcelQuotaStatus.SUSPENDED, ParcelQuotaStatus.CLOSED -> return true
                }
                return false
            }
            ParcelQuotaStatus.SUSPENDED -> return next == ParcelQuotaStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ParcelQuotaStatus) {
        if (!canMoveTo(next)) {
            error("parcel quota $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $title"

    fun effectiveLoad(): Double = round((margin - odometer.toDouble()) * 100) / 100

    fun snapshot(): ParcelQuota {
        return ParcelQuota(
            code = code,
            title = title,
            version = version,
            priority = priority,
            reviewer = reviewer,
            odometer = odometer,
            weight = weight,
            ceiling = ceiling,
            margin = margin,
            contactEmail = contactEmail,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a parcel quota in status draft. */
fun newParcelQuota(code: String, title: String): ParcelQuota {
    return ParcelQuota(
        code = code,
        title = title,
        version = 0,
        priority = 0,
        reviewer = "",
        odometer = 0,
        weight = 0.0,
        ceiling = 0.0,
        margin = 0.0,
        contactEmail = "",
        limit = 0,
        status = ParcelQuotaStatus.DRAFT,
    )
}
