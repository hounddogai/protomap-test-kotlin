package com.acme.transport.freightpay.ratecard.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global rate card baseline is in its life cycle. */
enum class GlobalRateCardBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global rate card baseline. Its code is unique within a tenant. */
data class GlobalRateCardBaseline(
    var code: String,
    var label: String,
    var endsAt: Int,
    var attributes: MutableMap<String, String>,
    var sequence: Int,
    var status: GlobalRateCardBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRateCardBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRateCardBaselineStatus): Boolean {
        when (status) {
            GlobalRateCardBaselineStatus.DRAFT -> return next == GlobalRateCardBaselineStatus.ACTIVE || next == GlobalRateCardBaselineStatus.CLOSED
            GlobalRateCardBaselineStatus.PENDING -> return next == GlobalRateCardBaselineStatus.ACTIVE || next == GlobalRateCardBaselineStatus.CLOSED
            GlobalRateCardBaselineStatus.ACTIVE -> return next == GlobalRateCardBaselineStatus.SUSPENDED || next == GlobalRateCardBaselineStatus.CLOSED
            GlobalRateCardBaselineStatus.SUSPENDED -> {
                when (next) {
                    GlobalRateCardBaselineStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalRateCardBaselineStatus) {
        if (!canMoveTo(next)) {
            error("global rate card baseline $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netLoad(): Double = round(endsAt.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): GlobalRateCardBaseline {
        return GlobalRateCardBaseline(
            code = code,
            label = label,
            endsAt = endsAt,
            attributes = attributes,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global rate card baseline in status draft. */
fun newGlobalRateCardBaseline(code: String, label: String): GlobalRateCardBaseline {
    return GlobalRateCardBaseline(
        code = code,
        label = label,
        endsAt = 0,
        attributes = mutableMapOf<String, String>(),
        sequence = 0,
        status = GlobalRateCardBaselineStatus.DRAFT,
    )
}
