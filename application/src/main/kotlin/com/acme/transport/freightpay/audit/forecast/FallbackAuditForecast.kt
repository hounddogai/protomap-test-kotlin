package com.acme.transport.freightpay.audit.forecast

import com.acme.transport.kit.text.normalize

// The fallback audit forecast records of freightpay's audit work, and the rules, stores, and calculations
// around them.

/** Where a fallback audit forecast is in its life cycle. */
enum class FallbackAuditForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A fallback audit forecast. Its code is unique within a tenant. */
data class FallbackAuditForecast(
    var code: String,
    var title: String,
    var region: String,
    var attributes: MutableMap<String, String>,
    var createdAt: Int,
    var status: FallbackAuditForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAuditForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAuditForecastStatus): Boolean {
        when (status) {
            FallbackAuditForecastStatus.CREATED -> {
                when (next) {
                    FallbackAuditForecastStatus.PUBLISHED -> return true
                }
                return false
            }
            FallbackAuditForecastStatus.VALIDATED -> return next == FallbackAuditForecastStatus.PUBLISHED || next == FallbackAuditForecastStatus.RETIRED
            FallbackAuditForecastStatus.PUBLISHED -> return next == FallbackAuditForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackAuditForecastStatus) {
        if (!canMoveTo(next)) {
            error("fallback audit forecast $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback audit forecast"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): FallbackAuditForecast {
        return FallbackAuditForecast(
            code = code,
            title = title,
            region = region,
            attributes = attributes,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback audit forecast in status created. */
fun newFallbackAuditForecast(code: String, title: String): FallbackAuditForecast {
    return FallbackAuditForecast(
        code = code,
        title = title,
        region = "",
        attributes = mutableMapOf<String, String>(),
        createdAt = 0,
        status = FallbackAuditForecastStatus.CREATED,
    )
}
