package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered tariff template is in its life cycle. */
enum class TieredTariffTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tiered tariff template as the tariff team keeps it. */
data class TieredTariffTemplate(
    var code: String,
    var title: String,
    var sequence: Int,
    var version: Int,
    var discount: Double,
    var budget: Double,
    var maximum: Double,
    var updatedAt: Int,
    var retries: Int,
    var routeCode: String,
    var status: TieredTariffTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredTariffTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredTariffTemplateStatus): Boolean {
        when (status) {
            TieredTariffTemplateStatus.CREATED -> {
                when (next) {
                    TieredTariffTemplateStatus.VALIDATED, TieredTariffTemplateStatus.PUBLISHED -> return true
                }
                return false
            }
            TieredTariffTemplateStatus.VALIDATED -> return next == TieredTariffTemplateStatus.PUBLISHED
            TieredTariffTemplateStatus.PUBLISHED -> {
                when (next) {
                    TieredTariffTemplateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredTariffTemplateStatus) {
        if (!canMoveTo(next)) {
            error("tiered tariff template $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"tiered tariff template"} $code"

    fun adjustedLoad(): Double = round((discount - retries.toDouble()) * 100) / 100

    fun snapshot(): TieredTariffTemplate {
        return TieredTariffTemplate(
            code = code,
            title = title,
            sequence = sequence,
            version = version,
            discount = discount,
            budget = budget,
            maximum = maximum,
            updatedAt = updatedAt,
            retries = retries,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tiered tariff template in status created. */
fun newTieredTariffTemplate(code: String, title: String): TieredTariffTemplate {
    return TieredTariffTemplate(
        code = code,
        title = title,
        sequence = 0,
        version = 0,
        discount = 0.0,
        budget = 0.0,
        maximum = 0.0,
        updatedAt = 0,
        retries = 0,
        routeCode = "",
        status = TieredTariffTemplateStatus.CREATED,
    )
}
