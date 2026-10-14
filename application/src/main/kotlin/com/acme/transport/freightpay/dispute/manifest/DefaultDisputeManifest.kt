package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default dispute manifest records of freightpay's dispute work, and the rules, stores, and calculations
// around them.

/** Where a default dispute manifest is in its life cycle. */
enum class DefaultDisputeManifestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A default dispute manifest. Its code is unique within a tenant. */
data class DefaultDisputeManifest(
    var code: String,
    var title: String,
    var label: String,
    var batchSize: Int,
    var pallets: Int,
    var updatedAt: Int,
    var pinned: Boolean,
    var status: DefaultDisputeManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDisputeManifestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDisputeManifestStatus): Boolean {
        when (status) {
            DefaultDisputeManifestStatus.REQUESTED -> {
                when (next) {
                    DefaultDisputeManifestStatus.GRANTED -> return true
                }
                return false
            }
            DefaultDisputeManifestStatus.GRANTED -> return next == DefaultDisputeManifestStatus.REVOKED
            DefaultDisputeManifestStatus.REVOKED -> {
                when (next) {
                    DefaultDisputeManifestStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDisputeManifestStatus) {
        if (!canMoveTo(next)) {
            error("default dispute manifest $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"default dispute manifest"} $code"

    fun adjustedValue(): Double = round((batchSize.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): DefaultDisputeManifest {
        return DefaultDisputeManifest(
            code = code,
            title = title,
            label = label,
            batchSize = batchSize,
            pallets = pallets,
            updatedAt = updatedAt,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default dispute manifest in status requested. */
fun newDefaultDisputeManifest(code: String, title: String): DefaultDisputeManifest {
    return DefaultDisputeManifest(
        code = code,
        title = title,
        label = "",
        batchSize = 0,
        pallets = 0,
        updatedAt = 0,
        pinned = false,
        status = DefaultDisputeManifestStatus.REQUESTED,
    )
}
