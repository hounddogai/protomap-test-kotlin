package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.text.normalize

/** Where a ev charge record is in its life cycle. */
enum class EvChargeRecordStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A ev charge record. Its code is unique within a tenant. */
data class EvChargeRecord(
    var code: String,
    var label: String,
    var active: Boolean,
    var country: String,
    var shiftCode: String,
    var amount: Double,
    var status: EvChargeRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeRecordStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeRecordStatus): Boolean {
        when (status) {
            EvChargeRecordStatus.PROPOSED -> {
                when (next) {
                    EvChargeRecordStatus.APPROVED, EvChargeRecordStatus.REJECTED -> return true
                }
                return false
            }
            EvChargeRecordStatus.APPROVED -> {
                when (next) {
                    EvChargeRecordStatus.REJECTED, EvChargeRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
            EvChargeRecordStatus.REJECTED -> {
                when (next) {
                    EvChargeRecordStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EvChargeRecordStatus) {
        if (!canMoveTo(next)) {
            error("ev charge record $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = amount

    fun snapshot(): EvChargeRecord {
        return EvChargeRecord(
            code = code,
            label = label,
            active = active,
            country = country,
            shiftCode = shiftCode,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a ev charge record in status proposed. */
fun newEvChargeRecord(code: String, label: String): EvChargeRecord {
    return EvChargeRecord(
        code = code,
        label = label,
        active = false,
        country = "",
        shiftCode = "",
        amount = 0.0,
        status = EvChargeRecordStatus.PROPOSED,
    )
}
