package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional dispute bundle is in its life cycle. */
enum class RegionalDisputeBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional dispute bundle. Its code is unique within a tenant. */
data class RegionalDisputeBundle(
    var code: String,
    var name: String,
    var capacity: Int,
    var fuelLiters: Double,
    var attributes: MutableMap<String, String>,
    var quantity: Int,
    var ceiling: Double,
    var retries: Int,
    var pallets: Int,
    var status: RegionalDisputeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDisputeBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDisputeBundleStatus): Boolean {
        when (status) {
            RegionalDisputeBundleStatus.DRAFT -> {
                when (next) {
                    RegionalDisputeBundleStatus.ACTIVE, RegionalDisputeBundleStatus.CLOSED -> return true
                }
                return false
            }
            RegionalDisputeBundleStatus.PENDING -> {
                when (next) {
                    RegionalDisputeBundleStatus.ACTIVE, RegionalDisputeBundleStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalDisputeBundleStatus.ACTIVE -> {
                when (next) {
                    RegionalDisputeBundleStatus.SUSPENDED, RegionalDisputeBundleStatus.CLOSED -> return true
                }
                return false
            }
            RegionalDisputeBundleStatus.SUSPENDED -> {
                when (next) {
                    RegionalDisputeBundleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDisputeBundleStatus) {
        if (!canMoveTo(next)) {
            error("regional dispute bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round((fuelLiters - pallets.toDouble()) * 100) / 100

    fun snapshot(): RegionalDisputeBundle {
        return RegionalDisputeBundle(
            code = code,
            name = name,
            capacity = capacity,
            fuelLiters = fuelLiters,
            attributes = attributes,
            quantity = quantity,
            ceiling = ceiling,
            retries = retries,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional dispute bundle in status draft. */
fun newRegionalDisputeBundle(code: String, name: String): RegionalDisputeBundle {
    return RegionalDisputeBundle(
        code = code,
        name = name,
        capacity = 0,
        fuelLiters = 0.0,
        attributes = mutableMapOf<String, String>(),
        quantity = 0,
        ceiling = 0.0,
        retries = 0,
        pallets = 0,
        status = RegionalDisputeBundleStatus.DRAFT,
    )
}
