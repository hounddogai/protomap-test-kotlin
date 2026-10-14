package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.kit.text.normalize

/** Where a tariff target is in its life cycle. */
enum class TariffTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tariff target. Its code is unique within a tenant. */
data class TariffTarget(
    var code: String,
    var title: String,
    var timezone: String,
    var attributes: MutableMap<String, String>,
    var routeCode: String,
    var score: Double,
    var status: TariffTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffTargetStatus): Boolean {
        when (status) {
            TariffTargetStatus.CREATED -> return next == TariffTargetStatus.PUBLISHED
            TariffTargetStatus.VALIDATED -> return next == TariffTargetStatus.PUBLISHED || next == TariffTargetStatus.RETIRED
            TariffTargetStatus.PUBLISHED -> {
                when (next) {
                    TariffTargetStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffTargetStatus) {
        if (!canMoveTo(next)) {
            error("tariff target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = score

    fun snapshot(): TariffTarget {
        return TariffTarget(
            code = code,
            title = title,
            timezone = timezone,
            attributes = attributes,
            routeCode = routeCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tariff target in status created. */
fun newTariffTarget(code: String, title: String): TariffTarget {
    return TariffTarget(
        code = code,
        title = title,
        timezone = "",
        attributes = mutableMapOf<String, String>(),
        routeCode = "",
        score = 0.0,
        status = TariffTargetStatus.CREATED,
    )
}
