package com.acme.transport.emissions.evcharge.manifest

import com.acme.transport.kit.text.normalize

/** Where a fallback ev charge record is in its life cycle. */
enum class FallbackEvChargeRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A fallback ev charge record. Its code is unique within a tenant. */
data class FallbackEvChargeRecord(
    var code: String,
    var title: String,
    var channel: String,
    var priority: Int,
    var description: String,
    var segment: String,
    var currency: String,
    var attributes: MutableMap<String, String>,
    var status: FallbackEvChargeRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackEvChargeRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackEvChargeRecordStatus): Boolean {
        when (status) {
            FallbackEvChargeRecordStatus.PROPOSED -> return next == FallbackEvChargeRecordStatus.REJECTED || next == FallbackEvChargeRecordStatus.WITHDRAWN
            FallbackEvChargeRecordStatus.APPROVED -> {
                when (next) {
                    FallbackEvChargeRecordStatus.REJECTED, FallbackEvChargeRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            FallbackEvChargeRecordStatus.REJECTED -> return next == FallbackEvChargeRecordStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: FallbackEvChargeRecordStatus) {
        if (!canMoveTo(next)) {
            error("fallback ev charge record $code cannot move from $status to $next")
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

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): FallbackEvChargeRecord {
        return FallbackEvChargeRecord(
            code = code,
            title = title,
            channel = channel,
            priority = priority,
            description = description,
            segment = segment,
            currency = currency,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback ev charge record in status proposed. */
fun newFallbackEvChargeRecord(code: String, title: String): FallbackEvChargeRecord {
    return FallbackEvChargeRecord(
        code = code,
        title = title,
        channel = "",
        priority = 0,
        description = "",
        segment = "",
        currency = "",
        attributes = mutableMapOf<String, String>(),
        status = FallbackEvChargeRecordStatus.PROPOSED,
    )
}
