package com.acme.transport.freightpay.freightinvoice.overrideitem

import com.acme.transport.kit.text.normalize

/** Where a global freight invoice rule is in its life cycle. */
enum class GlobalFreightInvoiceRuleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One global freight invoice rule, with what the planners need to act on it. */
data class GlobalFreightInvoiceRule(
    var code: String,
    var name: String,
    var currency: String,
    var trailerId: String,
    var etaMinutes: Int,
    var source: String,
    var checksum: String,
    var status: GlobalFreightInvoiceRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalFreightInvoiceRuleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalFreightInvoiceRuleStatus): Boolean {
        when (status) {
            GlobalFreightInvoiceRuleStatus.REQUESTED -> {
                when (next) {
                    GlobalFreightInvoiceRuleStatus.REVOKED -> return true
                }
                return false
            }
            GlobalFreightInvoiceRuleStatus.GRANTED -> {
                when (next) {
                    GlobalFreightInvoiceRuleStatus.REVOKED -> return true
                }
                return false
            }
            GlobalFreightInvoiceRuleStatus.REVOKED -> return next == GlobalFreightInvoiceRuleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalFreightInvoiceRuleStatus) {
        if (!canMoveTo(next)) {
            error("global freight invoice rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): GlobalFreightInvoiceRule {
        return GlobalFreightInvoiceRule(
            code = code,
            name = name,
            currency = currency,
            trailerId = trailerId,
            etaMinutes = etaMinutes,
            source = source,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a global freight invoice rule in status requested. */
fun newGlobalFreightInvoiceRule(code: String, name: String): GlobalFreightInvoiceRule {
    return GlobalFreightInvoiceRule(
        code = code,
        name = name,
        currency = "",
        trailerId = "",
        etaMinutes = 0,
        source = "",
        checksum = "",
        status = GlobalFreightInvoiceRuleStatus.REQUESTED,
    )
}
