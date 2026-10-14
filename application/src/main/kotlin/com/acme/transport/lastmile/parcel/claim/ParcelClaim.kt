package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The parcel claim records of lastmile's parcel work, and the rules, stores, and calculations around them.

/** Where a parcel claim is in its life cycle. */
enum class ParcelClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A parcel claim. Its code is unique within a tenant. */
data class ParcelClaim(
    var code: String,
    var name: String,
    var notes: String,
    var odometer: Int,
    var reviewer: String,
    var sequence: Int,
    var windowDays: Int,
    var description: String,
    var label: String,
    var externalRef: String,
    var summary: String,
    var status: ParcelClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelClaimStatus): Boolean {
        when (status) {
            ParcelClaimStatus.PLANNED -> {
                when (next) {
                    ParcelClaimStatus.SCHEDULED -> return true
                }
                return false
            }
            ParcelClaimStatus.SCHEDULED -> return next == ParcelClaimStatus.IN_PROGRESS || next == ParcelClaimStatus.COMPLETED
            ParcelClaimStatus.IN_PROGRESS -> {
                when (next) {
                    ParcelClaimStatus.CANCELLED -> return true
                }
                return false
            }
            ParcelClaimStatus.COMPLETED -> {
                when (next) {
                    ParcelClaimStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelClaimStatus) {
        if (!canMoveTo(next)) {
            error("parcel claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun adjustedLoad(): Double = round((windowDays.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): ParcelClaim {
        return ParcelClaim(
            code = code,
            name = name,
            notes = notes,
            odometer = odometer,
            reviewer = reviewer,
            sequence = sequence,
            windowDays = windowDays,
            description = description,
            label = label,
            externalRef = externalRef,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a parcel claim in status planned. */
fun newParcelClaim(code: String, name: String): ParcelClaim {
    return ParcelClaim(
        code = code,
        name = name,
        notes = "",
        odometer = 0,
        reviewer = "",
        sequence = 0,
        windowDays = 0,
        description = "",
        label = "",
        externalRef = "",
        summary = "",
        status = ParcelClaimStatus.PLANNED,
    )
}
