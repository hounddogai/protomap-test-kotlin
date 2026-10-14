package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped backhaul ledger is in its life cycle. */
enum class CappedBackhaulLedgerStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped backhaul ledger. Its code is unique within a tenant. */
data class CappedBackhaulLedger(
    var code: String,
    var title: String,
    var locale: String,
    var amount: Double,
    var discount: Double,
    var margin: Double,
    var ownerId: String,
    var retries: Int,
    var loadKg: Double,
    var phone: String,
    var revision: Int,
    var status: CappedBackhaulLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedBackhaulLedgerStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedBackhaulLedgerStatus): Boolean {
        when (status) {
            CappedBackhaulLedgerStatus.NEW -> {
                when (next) {
                    CappedBackhaulLedgerStatus.RUNNING -> return true
                }
                return false
            }
            CappedBackhaulLedgerStatus.QUEUED -> {
                when (next) {
                    CappedBackhaulLedgerStatus.DONE, CappedBackhaulLedgerStatus.FAILED -> return true
                }
                return false
            }
            CappedBackhaulLedgerStatus.RUNNING -> {
                when (next) {
                    CappedBackhaulLedgerStatus.DONE -> return true
                }
                return false
            }
            CappedBackhaulLedgerStatus.DONE -> return next == CappedBackhaulLedgerStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedBackhaulLedgerStatus) {
        if (!canMoveTo(next)) {
            error("capped backhaul ledger $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun weightedTotal(): Double = round((retries.toDouble() - amount) * 100) / 100

    fun snapshot(): CappedBackhaulLedger {
        return CappedBackhaulLedger(
            code = code,
            title = title,
            locale = locale,
            amount = amount,
            discount = discount,
            margin = margin,
            ownerId = ownerId,
            retries = retries,
            loadKg = loadKg,
            phone = phone,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a capped backhaul ledger in status new. */
fun newCappedBackhaulLedger(code: String, title: String): CappedBackhaulLedger {
    return CappedBackhaulLedger(
        code = code,
        title = title,
        locale = "",
        amount = 0.0,
        discount = 0.0,
        margin = 0.0,
        ownerId = "",
        retries = 0,
        loadKg = 0.0,
        phone = "",
        revision = 0,
        status = CappedBackhaulLedgerStatus.NEW,
    )
}
