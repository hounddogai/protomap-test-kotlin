package com.acme.transport.network.servicearea.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped service area profile is in its life cycle. */
enum class CappedServiceAreaProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A capped service area profile as the service area team keeps it. */
data class CappedServiceAreaProfile(
    var code: String,
    var name: String,
    var batchSize: Int,
    var distanceKm: Double,
    var channel: String,
    var cost: Double,
    var label: String,
    var endsAt: Int,
    var status: CappedServiceAreaProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedServiceAreaProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedServiceAreaProfileStatus): Boolean {
        when (status) {
            CappedServiceAreaProfileStatus.CREATED -> {
                when (next) {
                    CappedServiceAreaProfileStatus.PUBLISHED -> return true
                }
                return false
            }
            CappedServiceAreaProfileStatus.VALIDATED -> return next == CappedServiceAreaProfileStatus.PUBLISHED || next == CappedServiceAreaProfileStatus.RETIRED
            CappedServiceAreaProfileStatus.PUBLISHED -> {
                when (next) {
                    CappedServiceAreaProfileStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedServiceAreaProfileStatus) {
        if (!canMoveTo(next)) {
            error("capped service area profile $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun projectedTotal(): Double = round(cost * distanceKm * 100) / 100

    fun snapshot(): CappedServiceAreaProfile {
        return CappedServiceAreaProfile(
            code = code,
            name = name,
            batchSize = batchSize,
            distanceKm = distanceKm,
            channel = channel,
            cost = cost,
            label = label,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capped service area profile in status created. */
fun newCappedServiceAreaProfile(code: String, name: String): CappedServiceAreaProfile {
    return CappedServiceAreaProfile(
        code = code,
        name = name,
        batchSize = 0,
        distanceKm = 0.0,
        channel = "",
        cost = 0.0,
        label = "",
        endsAt = 0,
        status = CappedServiceAreaProfileStatus.CREATED,
    )
}
