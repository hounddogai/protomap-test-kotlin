package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report budget is in its life cycle. */
enum class CarbonReportBudgetStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A carbon report budget as the carbon report team keeps it. */
data class CarbonReportBudget(
    var code: String,
    var name: String,
    var category: String,
    var threshold: Double,
    var floorValue: Double,
    var batchSize: Int,
    var contactEmail: String,
    var phone: String,
    var description: String,
    var maximum: Double,
    var retries: Int,
    var status: CarbonReportBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportBudgetStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportBudgetStatus): Boolean {
        when (status) {
            CarbonReportBudgetStatus.REQUESTED -> return next == CarbonReportBudgetStatus.REVOKED || next == CarbonReportBudgetStatus.EXPIRED
            CarbonReportBudgetStatus.GRANTED -> return next == CarbonReportBudgetStatus.REVOKED || next == CarbonReportBudgetStatus.EXPIRED
            CarbonReportBudgetStatus.REVOKED -> return next == CarbonReportBudgetStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CarbonReportBudgetStatus) {
        if (!canMoveTo(next)) {
            error("carbon report budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netLoad(): Double = round((batchSize.toDouble() + floorValue) * 100) / 100

    fun snapshot(): CarbonReportBudget {
        return CarbonReportBudget(
            code = code,
            name = name,
            category = category,
            threshold = threshold,
            floorValue = floorValue,
            batchSize = batchSize,
            contactEmail = contactEmail,
            phone = phone,
            description = description,
            maximum = maximum,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a carbon report budget in status requested. */
fun newCarbonReportBudget(code: String, name: String): CarbonReportBudget {
    return CarbonReportBudget(
        code = code,
        name = name,
        category = "",
        threshold = 0.0,
        floorValue = 0.0,
        batchSize = 0,
        contactEmail = "",
        phone = "",
        description = "",
        maximum = 0.0,
        retries = 0,
        status = CarbonReportBudgetStatus.REQUESTED,
    )
}
