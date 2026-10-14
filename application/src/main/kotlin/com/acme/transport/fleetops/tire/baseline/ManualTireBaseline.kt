package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual tire baseline records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a manual tire baseline is in its life cycle. */
enum class ManualTireBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A manual tire baseline. Its code is unique within a tenant. */
data class ManualTireBaseline(
    var code: String,
    var title: String,
    var rate: Double,
    var carrier: String,
    var discount: Double,
    var pinned: Boolean,
    var batchSize: Int,
    var amount: Double,
    var pallets: Int,
    var checksum: String,
    var timezone: String,
    var status: ManualTireBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTireBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTireBaselineStatus): Boolean {
        when (status) {
            ManualTireBaselineStatus.REQUESTED -> {
                when (next) {
                    ManualTireBaselineStatus.GRANTED -> return true
                }
                return false
            }
            ManualTireBaselineStatus.GRANTED -> {
                when (next) {
                    ManualTireBaselineStatus.REVOKED -> return true
                }
                return false
            }
            ManualTireBaselineStatus.REVOKED -> {
                when (next) {
                    ManualTireBaselineStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTireBaselineStatus) {
        if (!canMoveTo(next)) {
            error("manual tire baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedLoad(): Double = round(batchSize.toDouble() * rate * 100) / 100

    fun snapshot(): ManualTireBaseline {
        return ManualTireBaseline(
            code = code,
            title = title,
            rate = rate,
            carrier = carrier,
            discount = discount,
            pinned = pinned,
            batchSize = batchSize,
            amount = amount,
            pallets = pallets,
            checksum = checksum,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual tire baseline in status requested. */
fun newManualTireBaseline(code: String, title: String): ManualTireBaseline {
    return ManualTireBaseline(
        code = code,
        title = title,
        rate = 0.0,
        carrier = "",
        discount = 0.0,
        pinned = false,
        batchSize = 0,
        amount = 0.0,
        pallets = 0,
        checksum = "",
        timezone = "",
        status = ManualTireBaselineStatus.REQUESTED,
    )
}
