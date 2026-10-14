package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary service plan profile is in its life cycle. */
enum class PrimaryServicePlanProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary service plan profile. Its code is unique within a tenant. */
data class PrimaryServicePlanProfile(
    var code: String,
    var label: String,
    var score: Double,
    var shiftCode: String,
    var etaMinutes: Int,
    var loadKg: Double,
    var discount: Double,
    var archived: Boolean,
    var capacity: Int,
    var ownerId: String,
    var startsAt: Int,
    var status: PrimaryServicePlanProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryServicePlanProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryServicePlanProfileStatus): Boolean {
        when (status) {
            PrimaryServicePlanProfileStatus.CREATED -> {
                when (next) {
                    PrimaryServicePlanProfileStatus.VALIDATED, PrimaryServicePlanProfileStatus.PUBLISHED -> return true
                }
                return false
            }
            PrimaryServicePlanProfileStatus.VALIDATED -> return next == PrimaryServicePlanProfileStatus.PUBLISHED || next == PrimaryServicePlanProfileStatus.RETIRED
            PrimaryServicePlanProfileStatus.PUBLISHED -> {
                when (next) {
                    PrimaryServicePlanProfileStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryServicePlanProfileStatus) {
        if (!canMoveTo(next)) {
            error("primary service plan profile $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"primary service plan profile"} $code"

    fun adjustedLoad(): Double = round(etaMinutes.toDouble() * score * 100) / 100

    fun snapshot(): PrimaryServicePlanProfile {
        return PrimaryServicePlanProfile(
            code = code,
            label = label,
            score = score,
            shiftCode = shiftCode,
            etaMinutes = etaMinutes,
            loadKg = loadKg,
            discount = discount,
            archived = archived,
            capacity = capacity,
            ownerId = ownerId,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary service plan profile in status created. */
fun newPrimaryServicePlanProfile(code: String, label: String): PrimaryServicePlanProfile {
    return PrimaryServicePlanProfile(
        code = code,
        label = label,
        score = 0.0,
        shiftCode = "",
        etaMinutes = 0,
        loadKg = 0.0,
        discount = 0.0,
        archived = false,
        capacity = 0,
        ownerId = "",
        startsAt = 0,
        status = PrimaryServicePlanProfileStatus.CREATED,
    )
}
