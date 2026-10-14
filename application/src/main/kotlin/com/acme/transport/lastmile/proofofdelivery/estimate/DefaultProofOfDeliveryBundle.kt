package com.acme.transport.lastmile.proofofdelivery.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default proof of delivery bundle is in its life cycle. */
enum class DefaultProofOfDeliveryBundleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default proof of delivery bundle. Its code is unique within a tenant. */
data class DefaultProofOfDeliveryBundle(
    var code: String,
    var name: String,
    var label: String,
    var active: Boolean,
    var tags: MutableList<String>,
    var amount: Double,
    var summary: String,
    var archived: Boolean,
    var weight: Double,
    var status: DefaultProofOfDeliveryBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultProofOfDeliveryBundleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultProofOfDeliveryBundleStatus): Boolean {
        when (status) {
            DefaultProofOfDeliveryBundleStatus.IDLE -> return next == DefaultProofOfDeliveryBundleStatus.DRAINING || next == DefaultProofOfDeliveryBundleStatus.STOPPED
            DefaultProofOfDeliveryBundleStatus.WARMING -> return next == DefaultProofOfDeliveryBundleStatus.STOPPED
            DefaultProofOfDeliveryBundleStatus.READY -> {
                when (next) {
                    DefaultProofOfDeliveryBundleStatus.STOPPED -> return true
                }
                return false
            }
            DefaultProofOfDeliveryBundleStatus.DRAINING -> {
                when (next) {
                    DefaultProofOfDeliveryBundleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultProofOfDeliveryBundleStatus) {
        if (!canMoveTo(next)) {
            error("default proof of delivery bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netValue(): Double = round((weight - amount) * 100) / 100

    fun snapshot(): DefaultProofOfDeliveryBundle {
        return DefaultProofOfDeliveryBundle(
            code = code,
            name = name,
            label = label,
            active = active,
            tags = tags,
            amount = amount,
            summary = summary,
            archived = archived,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default proof of delivery bundle in status idle. */
fun newDefaultProofOfDeliveryBundle(code: String, name: String): DefaultProofOfDeliveryBundle {
    return DefaultProofOfDeliveryBundle(
        code = code,
        name = name,
        label = "",
        active = false,
        tags = mutableListOf<String>(),
        amount = 0.0,
        summary = "",
        archived = false,
        weight = 0.0,
        status = DefaultProofOfDeliveryBundleStatus.IDLE,
    )
}
