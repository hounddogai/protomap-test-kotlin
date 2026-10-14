package com.acme.transport.customs.tariff.tariffscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default tariff target is in its life cycle. */
enum class DefaultTariffTargetStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default tariff target. Its code is unique within a tenant. */
data class DefaultTariffTarget(
    var code: String,
    var title: String,
    var rate: Double,
    var priority: Int,
    var carrier: String,
    var approved: Boolean,
    var status: DefaultTariffTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTariffTargetStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTariffTargetStatus): Boolean {
        when (status) {
            DefaultTariffTargetStatus.CREATED -> {
                when (next) {
                    DefaultTariffTargetStatus.PUBLISHED, DefaultTariffTargetStatus.RETIRED -> return true
                }
                return false
            }
            DefaultTariffTargetStatus.VALIDATED -> {
                when (next) {
                    DefaultTariffTargetStatus.PUBLISHED, DefaultTariffTargetStatus.RETIRED -> return true
                }
                return false
            }
            DefaultTariffTargetStatus.PUBLISHED -> return next == DefaultTariffTargetStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: DefaultTariffTargetStatus) {
        if (!canMoveTo(next)) {
            error("default tariff target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((rate + priority.toDouble()) * 100) / 100

    fun snapshot(): DefaultTariffTarget {
        return DefaultTariffTarget(
            code = code,
            title = title,
            rate = rate,
            priority = priority,
            carrier = carrier,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default tariff target in status created. */
fun newDefaultTariffTarget(code: String, title: String): DefaultTariffTarget {
    return DefaultTariffTarget(
        code = code,
        title = title,
        rate = 0.0,
        priority = 0,
        carrier = "",
        approved = false,
        status = DefaultTariffTargetStatus.CREATED,
    )
}
