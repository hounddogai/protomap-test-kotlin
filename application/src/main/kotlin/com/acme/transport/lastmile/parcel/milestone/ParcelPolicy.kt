package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a parcel policy is in its life cycle. */
enum class ParcelPolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A parcel policy. Its code is unique within a tenant. */
data class ParcelPolicy(
    var code: String,
    var label: String,
    var title: String,
    var dock: String,
    var startsAt: Int,
    var attempts: Int,
    var loadKg: Double,
    var updatedAt: Int,
    var distanceKm: Double,
    var status: ParcelPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelPolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelPolicyStatus): Boolean {
        when (status) {
            ParcelPolicyStatus.DRAFT -> {
                when (next) {
                    ParcelPolicyStatus.PENDING, ParcelPolicyStatus.CLOSED -> return true
                }
                return false
            }
            ParcelPolicyStatus.PENDING -> {
                when (next) {
                    ParcelPolicyStatus.SUSPENDED, ParcelPolicyStatus.CLOSED -> return true
                }
                return false
            }
            ParcelPolicyStatus.ACTIVE -> return next == ParcelPolicyStatus.SUSPENDED || next == ParcelPolicyStatus.CLOSED
            ParcelPolicyStatus.SUSPENDED -> {
                when (next) {
                    ParcelPolicyStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelPolicyStatus) {
        if (!canMoveTo(next)) {
            error("parcel policy $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"parcel policy"} $code"

    fun weightedTotal(): Double = round((loadKg + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ParcelPolicy {
        return ParcelPolicy(
            code = code,
            label = label,
            title = title,
            dock = dock,
            startsAt = startsAt,
            attempts = attempts,
            loadKg = loadKg,
            updatedAt = updatedAt,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a parcel policy in status draft. */
fun newParcelPolicy(code: String, label: String): ParcelPolicy {
    return ParcelPolicy(
        code = code,
        label = label,
        title = "",
        dock = "",
        startsAt = 0,
        attempts = 0,
        loadKg = 0.0,
        updatedAt = 0,
        distanceKm = 0.0,
        status = ParcelPolicyStatus.DRAFT,
    )
}
