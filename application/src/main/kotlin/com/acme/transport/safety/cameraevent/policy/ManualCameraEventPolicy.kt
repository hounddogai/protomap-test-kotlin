package com.acme.transport.safety.cameraevent.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual camera event policy records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a manual camera event policy is in its life cycle. */
enum class ManualCameraEventPolicyStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a manual camera event policy and its current status. */
data class ManualCameraEventPolicy(
    var code: String,
    var name: String,
    var source: String,
    var ceiling: Double,
    var vehicleId: String,
    var score: Double,
    var pallets: Int,
    var minimum: Double,
    var archived: Boolean,
    var timezone: String,
    var weight: Double,
    var status: ManualCameraEventPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualCameraEventPolicyStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualCameraEventPolicyStatus): Boolean {
        when (status) {
            ManualCameraEventPolicyStatus.CREATED -> return next == ManualCameraEventPolicyStatus.VALIDATED || next == ManualCameraEventPolicyStatus.PUBLISHED
            ManualCameraEventPolicyStatus.VALIDATED -> return next == ManualCameraEventPolicyStatus.PUBLISHED || next == ManualCameraEventPolicyStatus.RETIRED
            ManualCameraEventPolicyStatus.PUBLISHED -> return next == ManualCameraEventPolicyStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualCameraEventPolicyStatus) {
        if (!canMoveTo(next)) {
            error("manual camera event policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round(weight * score * 100) / 100

    fun snapshot(): ManualCameraEventPolicy {
        return ManualCameraEventPolicy(
            code = code,
            name = name,
            source = source,
            ceiling = ceiling,
            vehicleId = vehicleId,
            score = score,
            pallets = pallets,
            minimum = minimum,
            archived = archived,
            timezone = timezone,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a manual camera event policy in status created. */
fun newManualCameraEventPolicy(code: String, name: String): ManualCameraEventPolicy {
    return ManualCameraEventPolicy(
        code = code,
        name = name,
        source = "",
        ceiling = 0.0,
        vehicleId = "",
        score = 0.0,
        pallets = 0,
        minimum = 0.0,
        archived = false,
        timezone = "",
        weight = 0.0,
        status = ManualCameraEventPolicyStatus.CREATED,
    )
}
