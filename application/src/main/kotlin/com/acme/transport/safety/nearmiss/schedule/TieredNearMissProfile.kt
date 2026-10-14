package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered near miss profile is in its life cycle. */
enum class TieredNearMissProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tiered near miss profile. Its code is unique within a tenant. */
data class TieredNearMissProfile(
    var code: String,
    var label: String,
    var retries: Int,
    var startsAt: Int,
    var updatedAt: Int,
    var pinned: Boolean,
    var weight: Double,
    var capacity: Int,
    var status: TieredNearMissProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredNearMissProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredNearMissProfileStatus): Boolean {
        when (status) {
            TieredNearMissProfileStatus.CREATED -> {
                when (next) {
                    TieredNearMissProfileStatus.VALIDATED, TieredNearMissProfileStatus.PUBLISHED -> return true
                }
                return false
            }
            TieredNearMissProfileStatus.VALIDATED -> {
                when (next) {
                    TieredNearMissProfileStatus.PUBLISHED, TieredNearMissProfileStatus.RETIRED -> return true
                }
                return false
            }
            TieredNearMissProfileStatus.PUBLISHED -> return next == TieredNearMissProfileStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredNearMissProfileStatus) {
        if (!canMoveTo(next)) {
            error("tiered near miss profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"tiered near miss profile"} $code"

    fun netExposure(): Double = round((retries.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): TieredNearMissProfile {
        return TieredNearMissProfile(
            code = code,
            label = label,
            retries = retries,
            startsAt = startsAt,
            updatedAt = updatedAt,
            pinned = pinned,
            weight = weight,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tiered near miss profile in status created. */
fun newTieredNearMissProfile(code: String, label: String): TieredNearMissProfile {
    return TieredNearMissProfile(
        code = code,
        label = label,
        retries = 0,
        startsAt = 0,
        updatedAt = 0,
        pinned = false,
        weight = 0.0,
        capacity = 0,
        status = TieredNearMissProfileStatus.CREATED,
    )
}
