package com.acme.transport.freightpay.freightinvoice.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a freight invoice forecast is in its life cycle. */
enum class FreightInvoiceForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A freight invoice forecast. Its code is unique within a tenant. */
data class FreightInvoiceForecast(
    var code: String,
    var name: String,
    var discount: Double,
    var etaMinutes: Int,
    var endsAt: Int,
    var retries: Int,
    var description: String,
    var contactEmail: String,
    var status: FreightInvoiceForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FreightInvoiceForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: FreightInvoiceForecastStatus): Boolean {
        when (status) {
            FreightInvoiceForecastStatus.PROPOSED -> return next == FreightInvoiceForecastStatus.REJECTED
            FreightInvoiceForecastStatus.APPROVED -> {
                when (next) {
                    FreightInvoiceForecastStatus.REJECTED, FreightInvoiceForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
            FreightInvoiceForecastStatus.REJECTED -> {
                when (next) {
                    FreightInvoiceForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FreightInvoiceForecastStatus) {
        if (!canMoveTo(next)) {
            error("freight invoice forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun weightedVolume(): Double = round((retries.toDouble() - etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): FreightInvoiceForecast {
        return FreightInvoiceForecast(
            code = code,
            name = name,
            discount = discount,
            etaMinutes = etaMinutes,
            endsAt = endsAt,
            retries = retries,
            description = description,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a freight invoice forecast in status proposed. */
fun newFreightInvoiceForecast(code: String, name: String): FreightInvoiceForecast {
    return FreightInvoiceForecast(
        code = code,
        name = name,
        discount = 0.0,
        etaMinutes = 0,
        endsAt = 0,
        retries = 0,
        description = "",
        contactEmail = "",
        status = FreightInvoiceForecastStatus.PROPOSED,
    )
}
