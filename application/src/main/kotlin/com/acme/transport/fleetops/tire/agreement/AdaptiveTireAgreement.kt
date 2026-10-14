package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive tire agreement records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a adaptive tire agreement is in its life cycle. */
enum class AdaptiveTireAgreementStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive tire agreement as the tire team keeps it. */
data class AdaptiveTireAgreement(
    var code: String,
    var title: String,
    var budget: Double,
    var dock: String,
    var lane: String,
    var distanceKm: Double,
    var country: String,
    var endsAt: Int,
    var margin: Double,
    var startsAt: Int,
    var pinned: Boolean,
    var status: AdaptiveTireAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTireAgreementStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTireAgreementStatus): Boolean {
        when (status) {
            AdaptiveTireAgreementStatus.DRAFT -> {
                when (next) {
                    AdaptiveTireAgreementStatus.PENDING -> return true
                }
                return false
            }
            AdaptiveTireAgreementStatus.PENDING -> {
                when (next) {
                    AdaptiveTireAgreementStatus.ACTIVE, AdaptiveTireAgreementStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveTireAgreementStatus.ACTIVE -> {
                when (next) {
                    AdaptiveTireAgreementStatus.SUSPENDED, AdaptiveTireAgreementStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveTireAgreementStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveTireAgreementStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTireAgreementStatus) {
        if (!canMoveTo(next)) {
            error("adaptive tire agreement $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive tire agreement"} $code"

    fun projectedLoad(): Double = round((margin + budget) * 100) / 100

    fun snapshot(): AdaptiveTireAgreement {
        return AdaptiveTireAgreement(
            code = code,
            title = title,
            budget = budget,
            dock = dock,
            lane = lane,
            distanceKm = distanceKm,
            country = country,
            endsAt = endsAt,
            margin = margin,
            startsAt = startsAt,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive tire agreement in status draft. */
fun newAdaptiveTireAgreement(code: String, title: String): AdaptiveTireAgreement {
    return AdaptiveTireAgreement(
        code = code,
        title = title,
        budget = 0.0,
        dock = "",
        lane = "",
        distanceKm = 0.0,
        country = "",
        endsAt = 0,
        margin = 0.0,
        startsAt = 0,
        pinned = false,
        status = AdaptiveTireAgreementStatus.DRAFT,
    )
}
