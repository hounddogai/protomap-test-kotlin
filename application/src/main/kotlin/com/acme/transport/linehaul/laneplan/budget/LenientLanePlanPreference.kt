package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient lane plan preference is in its life cycle. */
enum class LenientLanePlanPreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a lenient lane plan preference and its current status. */
data class LenientLanePlanPreference(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var name: String,
    var trailerId: String,
    var shiftCode: String,
    var amount: Double,
    var description: String,
    var createdAt: Int,
    var status: LenientLanePlanPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientLanePlanPreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientLanePlanPreferenceStatus): Boolean {
        when (status) {
            LenientLanePlanPreferenceStatus.REQUESTED -> return next == LenientLanePlanPreferenceStatus.EXPIRED
            LenientLanePlanPreferenceStatus.GRANTED -> return next == LenientLanePlanPreferenceStatus.EXPIRED
            LenientLanePlanPreferenceStatus.REVOKED -> {
                when (next) {
                    LenientLanePlanPreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientLanePlanPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("lenient lane plan preference $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"lenient lane plan preference"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedTotal(): Double = round(createdAt.toDouble() * amount * 100) / 100

    fun snapshot(): LenientLanePlanPreference {
        return LenientLanePlanPreference(
            code = code,
            label = label,
            tags = tags,
            name = name,
            trailerId = trailerId,
            shiftCode = shiftCode,
            amount = amount,
            description = description,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient lane plan preference in status requested. */
fun newLenientLanePlanPreference(code: String, label: String): LenientLanePlanPreference {
    return LenientLanePlanPreference(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        name = "",
        trailerId = "",
        shiftCode = "",
        amount = 0.0,
        description = "",
        createdAt = 0,
        status = LenientLanePlanPreferenceStatus.REQUESTED,
    )
}
