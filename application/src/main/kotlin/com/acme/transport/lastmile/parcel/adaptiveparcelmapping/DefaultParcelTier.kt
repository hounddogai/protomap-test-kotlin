package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default parcel tier is in its life cycle. */
enum class DefaultParcelTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a default parcel tier and its current status. */
data class DefaultParcelTier(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var batchSize: Int,
    var archived: Boolean,
    var status: DefaultParcelTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultParcelTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultParcelTierStatus): Boolean {
        when (status) {
            DefaultParcelTierStatus.IDLE -> {
                when (next) {
                    DefaultParcelTierStatus.WARMING, DefaultParcelTierStatus.READY -> return true
                }
                return false
            }
            DefaultParcelTierStatus.WARMING -> return next == DefaultParcelTierStatus.DRAINING || next == DefaultParcelTierStatus.STOPPED
            DefaultParcelTierStatus.READY -> {
                when (next) {
                    DefaultParcelTierStatus.STOPPED -> return true
                }
                return false
            }
            DefaultParcelTierStatus.DRAINING -> return next == DefaultParcelTierStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DefaultParcelTierStatus) {
        if (!canMoveTo(next)) {
            error("default parcel tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round((distanceKm - batchSize.toDouble()) * 100) / 100

    fun snapshot(): DefaultParcelTier {
        return DefaultParcelTier(
            code = code,
            label = label,
            distanceKm = distanceKm,
            batchSize = batchSize,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default parcel tier in status idle. */
fun newDefaultParcelTier(code: String, label: String): DefaultParcelTier {
    return DefaultParcelTier(
        code = code,
        label = label,
        distanceKm = 0.0,
        batchSize = 0,
        archived = false,
        status = DefaultParcelTierStatus.IDLE,
    )
}
