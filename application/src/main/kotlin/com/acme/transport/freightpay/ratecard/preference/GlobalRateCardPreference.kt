package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global rate card preference records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a global rate card preference is in its life cycle. */
enum class GlobalRateCardPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a global rate card preference and its current status. */
data class GlobalRateCardPreference(
    var code: String,
    var name: String,
    var carrier: String,
    var active: Boolean,
    var country: String,
    var shiftCode: String,
    var attributes: MutableMap<String, String>,
    var revision: Int,
    var locale: String,
    var ratio: Double,
    var status: GlobalRateCardPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRateCardPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRateCardPreferenceStatus): Boolean {
        when (status) {
            GlobalRateCardPreferenceStatus.PROPOSED -> return next == GlobalRateCardPreferenceStatus.APPROVED
            GlobalRateCardPreferenceStatus.APPROVED -> {
                when (next) {
                    GlobalRateCardPreferenceStatus.REJECTED -> return true
                }
                return false
            }
            GlobalRateCardPreferenceStatus.REJECTED -> return next == GlobalRateCardPreferenceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalRateCardPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("global rate card preference $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netExposure(): Double = round((revision.toDouble() - ratio) * 100) / 100

    fun snapshot(): GlobalRateCardPreference {
        return GlobalRateCardPreference(
            code = code,
            name = name,
            carrier = carrier,
            active = active,
            country = country,
            shiftCode = shiftCode,
            attributes = attributes,
            revision = revision,
            locale = locale,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a global rate card preference in status proposed. */
fun newGlobalRateCardPreference(code: String, name: String): GlobalRateCardPreference {
    return GlobalRateCardPreference(
        code = code,
        name = name,
        carrier = "",
        active = false,
        country = "",
        shiftCode = "",
        attributes = mutableMapOf<String, String>(),
        revision = 0,
        locale = "",
        ratio = 0.0,
        status = GlobalRateCardPreferenceStatus.PROPOSED,
    )
}
