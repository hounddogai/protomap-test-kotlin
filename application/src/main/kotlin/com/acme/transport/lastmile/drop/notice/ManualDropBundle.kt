package com.acme.transport.lastmile.drop.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual drop bundle is in its life cycle. */
enum class ManualDropBundleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One manual drop bundle, with what the planners need to act on it. */
data class ManualDropBundle(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var quantity: Int,
    var graceDays: Int,
    var status: ManualDropBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualDropBundleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualDropBundleStatus): Boolean {
        when (status) {
            ManualDropBundleStatus.CREATED -> return next == ManualDropBundleStatus.VALIDATED
            ManualDropBundleStatus.VALIDATED -> {
                when (next) {
                    ManualDropBundleStatus.PUBLISHED, ManualDropBundleStatus.RETIRED -> return true
                }
                return false
            }
            ManualDropBundleStatus.PUBLISHED -> return next == ManualDropBundleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ManualDropBundleStatus) {
        if (!canMoveTo(next)) {
            error("manual drop bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netTotal(): Double = round(quantity.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): ManualDropBundle {
        return ManualDropBundle(
            code = code,
            name = name,
            pinned = pinned,
            quantity = quantity,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a manual drop bundle in status created. */
fun newManualDropBundle(code: String, name: String): ManualDropBundle {
    return ManualDropBundle(
        code = code,
        name = name,
        pinned = false,
        quantity = 0,
        graceDays = 0,
        status = ManualDropBundleStatus.CREATED,
    )
}
