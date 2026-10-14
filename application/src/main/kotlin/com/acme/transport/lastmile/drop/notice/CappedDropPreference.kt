package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped drop preference is in its life cycle. */
enum class CappedDropPreferenceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A capped drop preference as the drop team keeps it. */
data class CappedDropPreference(
    var code: String,
    var label: String,
    var depot: String,
    var quantity: Int,
    var phone: String,
    var vehicleId: String,
    var pallets: Int,
    var status: CappedDropPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDropPreferenceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDropPreferenceStatus): Boolean {
        when (status) {
            CappedDropPreferenceStatus.NEW -> return next == CappedDropPreferenceStatus.QUEUED || next == CappedDropPreferenceStatus.RUNNING
            CappedDropPreferenceStatus.QUEUED -> {
                when (next) {
                    CappedDropPreferenceStatus.RUNNING, CappedDropPreferenceStatus.DONE -> return true
                }
                return false
            }
            CappedDropPreferenceStatus.RUNNING -> return next == CappedDropPreferenceStatus.DONE || next == CappedDropPreferenceStatus.FAILED
            CappedDropPreferenceStatus.DONE -> return next == CappedDropPreferenceStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CappedDropPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("capped drop preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netTotal(): Double = round(quantity.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): CappedDropPreference {
        return CappedDropPreference(
            code = code,
            label = label,
            depot = depot,
            quantity = quantity,
            phone = phone,
            vehicleId = vehicleId,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a capped drop preference in status new. */
fun newCappedDropPreference(code: String, label: String): CappedDropPreference {
    return CappedDropPreference(
        code = code,
        label = label,
        depot = "",
        quantity = 0,
        phone = "",
        vehicleId = "",
        pallets = 0,
        status = CappedDropPreferenceStatus.NEW,
    )
}
