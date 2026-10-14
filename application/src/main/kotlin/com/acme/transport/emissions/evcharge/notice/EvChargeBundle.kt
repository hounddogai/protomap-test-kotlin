package com.acme.transport.emissions.evcharge.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a ev charge bundle is in its life cycle. */
enum class EvChargeBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A ev charge bundle as the ev charge team keeps it. */
data class EvChargeBundle(
    var code: String,
    var name: String,
    var pallets: Int,
    var active: Boolean,
    var discount: Double,
    var loadKg: Double,
    var floorValue: Double,
    var status: EvChargeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeBundleStatus): Boolean {
        when (status) {
            EvChargeBundleStatus.NEW -> {
                when (next) {
                    EvChargeBundleStatus.QUEUED -> return true
                }
                return false
            }
            EvChargeBundleStatus.QUEUED -> return next == EvChargeBundleStatus.DONE
            EvChargeBundleStatus.RUNNING -> {
                when (next) {
                    EvChargeBundleStatus.DONE, EvChargeBundleStatus.FAILED -> return true
                }
                return false
            }
            EvChargeBundleStatus.DONE -> return next == EvChargeBundleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: EvChargeBundleStatus) {
        if (!canMoveTo(next)) {
            error("ev charge bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedVolume(): Double = round((loadKg - pallets.toDouble()) * 100) / 100

    fun snapshot(): EvChargeBundle {
        return EvChargeBundle(
            code = code,
            name = name,
            pallets = pallets,
            active = active,
            discount = discount,
            loadKg = loadKg,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a ev charge bundle in status new. */
fun newEvChargeBundle(code: String, name: String): EvChargeBundle {
    return EvChargeBundle(
        code = code,
        name = name,
        pallets = 0,
        active = false,
        discount = 0.0,
        loadKg = 0.0,
        floorValue = 0.0,
        status = EvChargeBundleStatus.NEW,
    )
}
