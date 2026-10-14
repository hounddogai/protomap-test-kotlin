package com.acme.transport.customs.hold.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold bundle records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold bundle is in its life cycle. */
enum class HoldBundleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a hold bundle and its current status. */
data class HoldBundle(
    var code: String,
    var title: String,
    var margin: Double,
    var budget: Double,
    var endsAt: Int,
    var contactEmail: String,
    var status: HoldBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldBundleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldBundleStatus): Boolean {
        when (status) {
            HoldBundleStatus.DRAFT -> return next == HoldBundleStatus.PENDING
            HoldBundleStatus.PENDING -> return next == HoldBundleStatus.ACTIVE || next == HoldBundleStatus.CLOSED
            HoldBundleStatus.ACTIVE -> return next == HoldBundleStatus.CLOSED
            HoldBundleStatus.SUSPENDED -> return next == HoldBundleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: HoldBundleStatus) {
        if (!canMoveTo(next)) {
            error("hold bundle $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun netTotal(): Double = round((budget + endsAt.toDouble()) * 100) / 100

    fun snapshot(): HoldBundle {
        return HoldBundle(
            code = code,
            title = title,
            margin = margin,
            budget = budget,
            endsAt = endsAt,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a hold bundle in status draft. */
fun newHoldBundle(code: String, title: String): HoldBundle {
    return HoldBundle(
        code = code,
        title = title,
        margin = 0.0,
        budget = 0.0,
        endsAt = 0,
        contactEmail = "",
        status = HoldBundleStatus.DRAFT,
    )
}
