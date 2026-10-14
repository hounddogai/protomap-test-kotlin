package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.text.normalize

/** Where a adaptive tariff slot is in its life cycle. */
enum class AdaptiveTariffSlotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A adaptive tariff slot. Its code is unique within a tenant. */
data class AdaptiveTariffSlot(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var notes: String,
    var capacity: Int,
    var externalRef: String,
    var dock: String,
    var status: AdaptiveTariffSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveTariffSlotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveTariffSlotStatus): Boolean {
        when (status) {
            AdaptiveTariffSlotStatus.NEW -> {
                when (next) {
                    AdaptiveTariffSlotStatus.QUEUED -> return true
                }
                return false
            }
            AdaptiveTariffSlotStatus.QUEUED -> return next == AdaptiveTariffSlotStatus.DONE || next == AdaptiveTariffSlotStatus.FAILED
            AdaptiveTariffSlotStatus.RUNNING -> return next == AdaptiveTariffSlotStatus.DONE || next == AdaptiveTariffSlotStatus.FAILED
            AdaptiveTariffSlotStatus.DONE -> {
                when (next) {
                    AdaptiveTariffSlotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveTariffSlotStatus) {
        if (!canMoveTo(next)) {
            error("adaptive tariff slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): AdaptiveTariffSlot {
        return AdaptiveTariffSlot(
            code = code,
            title = title,
            attributes = attributes,
            notes = notes,
            capacity = capacity,
            externalRef = externalRef,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive tariff slot in status new. */
fun newAdaptiveTariffSlot(code: String, title: String): AdaptiveTariffSlot {
    return AdaptiveTariffSlot(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        notes = "",
        capacity = 0,
        externalRef = "",
        dock = "",
        status = AdaptiveTariffSlotStatus.NEW,
    )
}
