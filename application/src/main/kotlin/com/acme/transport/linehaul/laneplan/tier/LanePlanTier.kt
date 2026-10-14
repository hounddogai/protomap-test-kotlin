package com.acme.transport.linehaul.laneplan.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lane plan tier records of linehaul's lane plan work, and the rules, stores, and calculations around
// them.

/** Where a lane plan tier is in its life cycle. */
enum class LanePlanTierStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lane plan tier as the lane plan team keeps it. */
data class LanePlanTier(
    var code: String,
    var name: String,
    var source: String,
    var shiftCode: String,
    var contactEmail: String,
    var updatedAt: Int,
    var phone: String,
    var active: Boolean,
    var floorValue: Double,
    var score: Double,
    var revision: Int,
    var status: LanePlanTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LanePlanTierStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LanePlanTierStatus): Boolean {
        when (status) {
            LanePlanTierStatus.NEW -> {
                when (next) {
                    LanePlanTierStatus.RUNNING -> return true
                }
                return false
            }
            LanePlanTierStatus.QUEUED -> return next == LanePlanTierStatus.RUNNING || next == LanePlanTierStatus.DONE
            LanePlanTierStatus.RUNNING -> {
                when (next) {
                    LanePlanTierStatus.DONE -> return true
                }
                return false
            }
            LanePlanTierStatus.DONE -> {
                when (next) {
                    LanePlanTierStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LanePlanTierStatus) {
        if (!canMoveTo(next)) {
            error("lane plan tier $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun projectedExposure(): Double = round((score - revision.toDouble()) * 100) / 100

    fun snapshot(): LanePlanTier {
        return LanePlanTier(
            code = code,
            name = name,
            source = source,
            shiftCode = shiftCode,
            contactEmail = contactEmail,
            updatedAt = updatedAt,
            phone = phone,
            active = active,
            floorValue = floorValue,
            score = score,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lane plan tier in status new. */
fun newLanePlanTier(code: String, name: String): LanePlanTier {
    return LanePlanTier(
        code = code,
        name = name,
        source = "",
        shiftCode = "",
        contactEmail = "",
        updatedAt = 0,
        phone = "",
        active = false,
        floorValue = 0.0,
        score = 0.0,
        revision = 0,
        status = LanePlanTierStatus.NEW,
    )
}
