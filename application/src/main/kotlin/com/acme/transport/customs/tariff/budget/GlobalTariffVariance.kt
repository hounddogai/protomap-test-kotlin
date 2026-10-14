package com.acme.transport.customs.tariff.budget

import com.acme.transport.kit.text.normalize

/** Where a global tariff variance is in its life cycle. */
enum class GlobalTariffVarianceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One global tariff variance, with what the planners need to act on it. */
data class GlobalTariffVariance(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var timezone: String,
    var checksum: String,
    var pallets: Int,
    var status: GlobalTariffVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTariffVarianceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTariffVarianceStatus): Boolean {
        when (status) {
            GlobalTariffVarianceStatus.DRAFT -> return next == GlobalTariffVarianceStatus.ACTIVE
            GlobalTariffVarianceStatus.PENDING -> {
                when (next) {
                    GlobalTariffVarianceStatus.SUSPENDED -> return true
                }
                return false
            }
            GlobalTariffVarianceStatus.ACTIVE -> return next == GlobalTariffVarianceStatus.CLOSED
            GlobalTariffVarianceStatus.SUSPENDED -> return next == GlobalTariffVarianceStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: GlobalTariffVarianceStatus) {
        if (!canMoveTo(next)) {
            error("global tariff variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global tariff variance"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): GlobalTariffVariance {
        return GlobalTariffVariance(
            code = code,
            label = label,
            tags = tags,
            timezone = timezone,
            checksum = checksum,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global tariff variance in status draft. */
fun newGlobalTariffVariance(code: String, label: String): GlobalTariffVariance {
    return GlobalTariffVariance(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        timezone = "",
        checksum = "",
        pallets = 0,
        status = GlobalTariffVarianceStatus.DRAFT,
    )
}
