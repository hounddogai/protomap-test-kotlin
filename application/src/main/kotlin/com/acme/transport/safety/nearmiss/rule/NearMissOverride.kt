package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a near miss override is in its life cycle. */
enum class NearMissOverrideStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A near miss override as the near miss team keeps it. */
data class NearMissOverride(
    var code: String,
    var label: String,
    var active: Boolean,
    var reviewer: String,
    var ownerId: String,
    var quantity: Int,
    var batchSize: Int,
    var priority: Int,
    var timezone: String,
    var status: NearMissOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissOverrideStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissOverrideStatus): Boolean {
        when (status) {
            NearMissOverrideStatus.CREATED -> return next == NearMissOverrideStatus.PUBLISHED || next == NearMissOverrideStatus.RETIRED
            NearMissOverrideStatus.VALIDATED -> return next == NearMissOverrideStatus.PUBLISHED
            NearMissOverrideStatus.PUBLISHED -> {
                when (next) {
                    NearMissOverrideStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NearMissOverrideStatus) {
        if (!canMoveTo(next)) {
            error("near miss override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"near miss override"} $code"

    fun netTotal(): Double = round((priority.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): NearMissOverride {
        return NearMissOverride(
            code = code,
            label = label,
            active = active,
            reviewer = reviewer,
            ownerId = ownerId,
            quantity = quantity,
            batchSize = batchSize,
            priority = priority,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a near miss override in status created. */
fun newNearMissOverride(code: String, label: String): NearMissOverride {
    return NearMissOverride(
        code = code,
        label = label,
        active = false,
        reviewer = "",
        ownerId = "",
        quantity = 0,
        batchSize = 0,
        priority = 0,
        timezone = "",
        status = NearMissOverrideStatus.CREATED,
    )
}
