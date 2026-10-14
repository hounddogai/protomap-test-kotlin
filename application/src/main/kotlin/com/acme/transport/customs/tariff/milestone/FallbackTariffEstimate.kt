package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback tariff estimate is in its life cycle. */
enum class FallbackTariffEstimateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback tariff estimate as the tariff team keeps it. */
data class FallbackTariffEstimate(
    var code: String,
    var title: String,
    var phone: String,
    var ownerId: String,
    var active: Boolean,
    var version: Int,
    var segment: String,
    var revision: Int,
    var pinned: Boolean,
    var capacity: Int,
    var driverId: String,
    var status: FallbackTariffEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackTariffEstimateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackTariffEstimateStatus): Boolean {
        when (status) {
            FallbackTariffEstimateStatus.NEW -> {
                when (next) {
                    FallbackTariffEstimateStatus.RUNNING -> return true
                }
                return false
            }
            FallbackTariffEstimateStatus.QUEUED -> return next == FallbackTariffEstimateStatus.RUNNING || next == FallbackTariffEstimateStatus.FAILED
            FallbackTariffEstimateStatus.RUNNING -> return next == FallbackTariffEstimateStatus.DONE || next == FallbackTariffEstimateStatus.FAILED
            FallbackTariffEstimateStatus.DONE -> {
                when (next) {
                    FallbackTariffEstimateStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackTariffEstimateStatus) {
        if (!canMoveTo(next)) {
            error("fallback tariff estimate $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedExposure(): Double = round((revision.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): FallbackTariffEstimate {
        return FallbackTariffEstimate(
            code = code,
            title = title,
            phone = phone,
            ownerId = ownerId,
            active = active,
            version = version,
            segment = segment,
            revision = revision,
            pinned = pinned,
            capacity = capacity,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fallback tariff estimate in status new. */
fun newFallbackTariffEstimate(code: String, title: String): FallbackTariffEstimate {
    return FallbackTariffEstimate(
        code = code,
        title = title,
        phone = "",
        ownerId = "",
        active = false,
        version = 0,
        segment = "",
        revision = 0,
        pinned = false,
        capacity = 0,
        driverId = "",
        status = FallbackTariffEstimateStatus.NEW,
    )
}
