package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary attempt bundle is in its life cycle. */
enum class PrimaryAttemptBundleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A primary attempt bundle as the attempt team keeps it. */
data class PrimaryAttemptBundle(
    var code: String,
    var title: String,
    var attributes: MutableMap<String, String>,
    var visible: Boolean,
    var driverId: String,
    var quantity: Int,
    var currency: String,
    var batchSize: Int,
    var budget: Double,
    var status: PrimaryAttemptBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAttemptBundleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAttemptBundleStatus): Boolean {
        when (status) {
            PrimaryAttemptBundleStatus.PLANNED -> return next == PrimaryAttemptBundleStatus.CANCELLED
            PrimaryAttemptBundleStatus.SCHEDULED -> return next == PrimaryAttemptBundleStatus.CANCELLED
            PrimaryAttemptBundleStatus.IN_PROGRESS -> {
                when (next) {
                    PrimaryAttemptBundleStatus.COMPLETED, PrimaryAttemptBundleStatus.CANCELLED -> return true
                }
                return false
            }
            PrimaryAttemptBundleStatus.COMPLETED -> {
                when (next) {
                    PrimaryAttemptBundleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryAttemptBundleStatus) {
        if (!canMoveTo(next)) {
            error("primary attempt bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary attempt bundle"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netTotal(): Double = round((batchSize.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): PrimaryAttemptBundle {
        return PrimaryAttemptBundle(
            code = code,
            title = title,
            attributes = attributes,
            visible = visible,
            driverId = driverId,
            quantity = quantity,
            currency = currency,
            batchSize = batchSize,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary attempt bundle in status planned. */
fun newPrimaryAttemptBundle(code: String, title: String): PrimaryAttemptBundle {
    return PrimaryAttemptBundle(
        code = code,
        title = title,
        attributes = mutableMapOf<String, String>(),
        visible = false,
        driverId = "",
        quantity = 0,
        currency = "",
        batchSize = 0,
        budget = 0.0,
        status = PrimaryAttemptBundleStatus.PLANNED,
    )
}
