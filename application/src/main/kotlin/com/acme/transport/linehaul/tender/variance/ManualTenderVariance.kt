package com.acme.transport.linehaul.tender.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual tender variance records of linehaul's tender work, and the rules, stores, and calculations
// around them.

/** Where a manual tender variance is in its life cycle. */
enum class ManualTenderVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A manual tender variance. Its code is unique within a tenant. */
data class ManualTenderVariance(
    var code: String,
    var label: String,
    var budget: Double,
    var currency: String,
    var region: String,
    var vehicleId: String,
    var distanceKm: Double,
    var locale: String,
    var limit: Int,
    var checksum: String,
    var rate: Double,
    var status: ManualTenderVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTenderVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTenderVarianceStatus): Boolean {
        when (status) {
            ManualTenderVarianceStatus.CREATED -> {
                when (next) {
                    ManualTenderVarianceStatus.PUBLISHED, ManualTenderVarianceStatus.RETIRED -> return true
                }
                return false
            }
            ManualTenderVarianceStatus.VALIDATED -> {
                when (next) {
                    ManualTenderVarianceStatus.RETIRED -> return true
                }
                return false
            }
            ManualTenderVarianceStatus.PUBLISHED -> {
                when (next) {
                    ManualTenderVarianceStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTenderVarianceStatus) {
        if (!canMoveTo(next)) {
            error("manual tender variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual tender variance"} $code"

    fun projectedVolume(): Double = round(limit.toDouble() * rate * 100) / 100

    fun snapshot(): ManualTenderVariance {
        return ManualTenderVariance(
            code = code,
            label = label,
            budget = budget,
            currency = currency,
            region = region,
            vehicleId = vehicleId,
            distanceKm = distanceKm,
            locale = locale,
            limit = limit,
            checksum = checksum,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual tender variance in status created. */
fun newManualTenderVariance(code: String, label: String): ManualTenderVariance {
    return ManualTenderVariance(
        code = code,
        label = label,
        budget = 0.0,
        currency = "",
        region = "",
        vehicleId = "",
        distanceKm = 0.0,
        locale = "",
        limit = 0,
        checksum = "",
        rate = 0.0,
        status = ManualTenderVarianceStatus.CREATED,
    )
}
