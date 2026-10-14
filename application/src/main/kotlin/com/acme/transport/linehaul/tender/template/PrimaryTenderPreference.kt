package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary tender preference is in its life cycle. */
enum class PrimaryTenderPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a primary tender preference and its current status. */
data class PrimaryTenderPreference(
    var code: String,
    var title: String,
    var rate: Double,
    var summary: String,
    var fuelLiters: Double,
    var retries: Int,
    var discount: Double,
    var distanceKm: Double,
    var carrier: String,
    var locale: String,
    var budget: Double,
    var status: PrimaryTenderPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTenderPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTenderPreferenceStatus): Boolean {
        when (status) {
            PrimaryTenderPreferenceStatus.REQUESTED -> {
                when (next) {
                    PrimaryTenderPreferenceStatus.GRANTED, PrimaryTenderPreferenceStatus.REVOKED -> return true
                }
                return false
            }
            PrimaryTenderPreferenceStatus.GRANTED -> {
                when (next) {
                    PrimaryTenderPreferenceStatus.REVOKED -> return true
                }
                return false
            }
            PrimaryTenderPreferenceStatus.REVOKED -> return next == PrimaryTenderPreferenceStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: PrimaryTenderPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("primary tender preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary tender preference"} $code"

    fun effectiveValue(): Double = round((discount + retries.toDouble()) * 100) / 100

    fun snapshot(): PrimaryTenderPreference {
        return PrimaryTenderPreference(
            code = code,
            title = title,
            rate = rate,
            summary = summary,
            fuelLiters = fuelLiters,
            retries = retries,
            discount = discount,
            distanceKm = distanceKm,
            carrier = carrier,
            locale = locale,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a primary tender preference in status requested. */
fun newPrimaryTenderPreference(code: String, title: String): PrimaryTenderPreference {
    return PrimaryTenderPreference(
        code = code,
        title = title,
        rate = 0.0,
        summary = "",
        fuelLiters = 0.0,
        retries = 0,
        discount = 0.0,
        distanceKm = 0.0,
        carrier = "",
        locale = "",
        budget = 0.0,
        status = PrimaryTenderPreferenceStatus.REQUESTED,
    )
}
