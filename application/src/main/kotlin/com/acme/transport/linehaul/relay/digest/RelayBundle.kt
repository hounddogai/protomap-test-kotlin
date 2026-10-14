package com.acme.transport.linehaul.relay.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a relay bundle is in its life cycle. */
enum class RelayBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A relay bundle as the relay team keeps it. */
data class RelayBundle(
    var code: String,
    var name: String,
    var contactEmail: String,
    var phone: String,
    var sequence: Int,
    var createdAt: Int,
    var dock: String,
    var ownerId: String,
    var driverId: String,
    var pallets: Int,
    var status: RelayBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RelayBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RelayBundleStatus): Boolean {
        when (status) {
            RelayBundleStatus.REQUESTED -> {
                when (next) {
                    RelayBundleStatus.GRANTED -> return true
                }
                return false
            }
            RelayBundleStatus.GRANTED -> return next == RelayBundleStatus.EXPIRED
            RelayBundleStatus.REVOKED -> return next == RelayBundleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RelayBundleStatus) {
        if (!canMoveTo(next)) {
            error("relay bundle $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"relay bundle"} $code"

    fun weightedTotal(): Double = round(sequence.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): RelayBundle {
        return RelayBundle(
            code = code,
            name = name,
            contactEmail = contactEmail,
            phone = phone,
            sequence = sequence,
            createdAt = createdAt,
            dock = dock,
            ownerId = ownerId,
            driverId = driverId,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a relay bundle in status requested. */
fun newRelayBundle(code: String, name: String): RelayBundle {
    return RelayBundle(
        code = code,
        name = name,
        contactEmail = "",
        phone = "",
        sequence = 0,
        createdAt = 0,
        dock = "",
        ownerId = "",
        driverId = "",
        pallets = 0,
        status = RelayBundleStatus.REQUESTED,
    )
}
