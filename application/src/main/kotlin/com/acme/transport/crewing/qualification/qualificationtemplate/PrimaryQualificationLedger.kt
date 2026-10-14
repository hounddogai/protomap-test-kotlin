package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary qualification ledger is in its life cycle. */
enum class PrimaryQualificationLedgerStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A primary qualification ledger. Its code is unique within a tenant. */
data class PrimaryQualificationLedger(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var vehicleId: String,
    var quantity: Int,
    var routeCode: String,
    var etaMinutes: Int,
    var status: PrimaryQualificationLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryQualificationLedgerStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryQualificationLedgerStatus): Boolean {
        when (status) {
            PrimaryQualificationLedgerStatus.CREATED -> {
                when (next) {
                    PrimaryQualificationLedgerStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryQualificationLedgerStatus.VALIDATED -> {
                when (next) {
                    PrimaryQualificationLedgerStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryQualificationLedgerStatus.PUBLISHED -> {
                when (next) {
                    PrimaryQualificationLedgerStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryQualificationLedgerStatus) {
        if (!canMoveTo(next)) {
            error("primary qualification ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedLoad(): Double = round(etaMinutes.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): PrimaryQualificationLedger {
        return PrimaryQualificationLedger(
            code = code,
            title = title,
            attributes = attributes,
            vehicleId = vehicleId,
            quantity = quantity,
            routeCode = routeCode,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary qualification ledger in status created. */
fun newPrimaryQualificationLedger(code: String, title: String): PrimaryQualificationLedger {
    return PrimaryQualificationLedger(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        vehicleId = "",
        quantity = 0,
        routeCode = "",
        etaMinutes = 0,
        status = PrimaryQualificationLedgerStatus.CREATED,
    )
}
