package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.text.normalize

// The capped ev charge preference records of emissions's ev charge work, and the rules, stores, and
// calculations around them.

/** Where a capped ev charge preference is in its life cycle. */
enum class CappedEvChargePreferenceStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capped ev charge preference as the ev charge team keeps it. */
data class CappedEvChargePreference(
    var code: String,
    var label: String,
    var dock: String,
    var unit: String,
    var carrier: String,
    var locale: String,
    var attributes: MutableMap<String, String>,
    var quantity: Int,
    var status: CappedEvChargePreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedEvChargePreferenceStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedEvChargePreferenceStatus): Boolean {
        when (status) {
            CappedEvChargePreferenceStatus.REQUESTED -> return next == CappedEvChargePreferenceStatus.EXPIRED
            CappedEvChargePreferenceStatus.GRANTED -> return next == CappedEvChargePreferenceStatus.REVOKED || next == CappedEvChargePreferenceStatus.EXPIRED
            CappedEvChargePreferenceStatus.REVOKED -> {
                when (next) {
                    CappedEvChargePreferenceStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedEvChargePreferenceStatus) {
        if (!canMoveTo(next)) {
            error("capped ev charge preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"capped ev charge preference"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): CappedEvChargePreference {
        return CappedEvChargePreference(
            code = code,
            label = label,
            dock = dock,
            unit = unit,
            carrier = carrier,
            locale = locale,
            attributes = attributes,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a capped ev charge preference in status requested. */
fun newCappedEvChargePreference(code: String, label: String): CappedEvChargePreference {
    return CappedEvChargePreference(
        code = code,
        label = label,
        dock = "",
        unit = "",
        carrier = "",
        locale = "",
        attributes = mutableMapOf<String, String>(),
        quantity = 0,
        status = CappedEvChargePreferenceStatus.REQUESTED,
    )
}
