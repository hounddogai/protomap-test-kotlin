package com.acme.transport.customs.tariff.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tariff policy records of customs's tariff work, and the rules, stores, and calculations around them.

/** Where a tariff policy is in its life cycle. */
enum class TariffPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A tariff policy as the tariff team keeps it. */
data class TariffPolicy(
    var code: String,
    var title: String,
    var pallets: Int,
    var sequence: Int,
    var label: String,
    var cost: Double,
    var ownerId: String,
    var status: TariffPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TariffPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: TariffPolicyStatus): Boolean {
        when (status) {
            TariffPolicyStatus.NEW -> {
                when (next) {
                    TariffPolicyStatus.QUEUED, TariffPolicyStatus.DONE -> return true
                }
                return false
            }
            TariffPolicyStatus.QUEUED -> return next == TariffPolicyStatus.FAILED
            TariffPolicyStatus.RUNNING -> {
                when (next) {
                    TariffPolicyStatus.FAILED -> return true
                }
                return false
            }
            TariffPolicyStatus.DONE -> {
                when (next) {
                    TariffPolicyStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TariffPolicyStatus) {
        if (!canMoveTo(next)) {
            error("tariff policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun projectedVolume(): Double = round((sequence.toDouble() - cost) * 100) / 100

    fun snapshot(): TariffPolicy {
        return TariffPolicy(
            code = code,
            title = title,
            pallets = pallets,
            sequence = sequence,
            label = label,
            cost = cost,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a tariff policy in status new. */
fun newTariffPolicy(code: String, title: String): TariffPolicy {
    return TariffPolicy(
        code = code,
        title = title,
        pallets = 0,
        sequence = 0,
        label = "",
        cost = 0.0,
        ownerId = "",
        status = TariffPolicyStatus.NEW,
    )
}
