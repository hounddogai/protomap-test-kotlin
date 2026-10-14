package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default drop draft is in its life cycle. */
enum class DefaultDropDraftStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A default drop draft as the drop team keeps it. */
data class DefaultDropDraft(
    var code: String,
    var label: String,
    var ceiling: Double,
    var carrier: String,
    var discount: Double,
    var trailerId: String,
    var status: DefaultDropDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultDropDraftStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultDropDraftStatus): Boolean {
        when (status) {
            DefaultDropDraftStatus.CREATED -> return next == DefaultDropDraftStatus.PUBLISHED || next == DefaultDropDraftStatus.RETIRED
            DefaultDropDraftStatus.VALIDATED -> {
                when (next) {
                    DefaultDropDraftStatus.PUBLISHED, DefaultDropDraftStatus.RETIRED -> return true
                }
                return false
            }
            DefaultDropDraftStatus.PUBLISHED -> {
                when (next) {
                    DefaultDropDraftStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultDropDraftStatus) {
        if (!canMoveTo(next)) {
            error("default drop draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"default drop draft"} $code"

    fun effectiveTotal(): Double = round((discount + ceiling) * 100) / 100

    fun snapshot(): DefaultDropDraft {
        return DefaultDropDraft(
            code = code,
            label = label,
            ceiling = ceiling,
            carrier = carrier,
            discount = discount,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a default drop draft in status created. */
fun newDefaultDropDraft(code: String, label: String): DefaultDropDraft {
    return DefaultDropDraft(
        code = code,
        label = label,
        ceiling = 0.0,
        carrier = "",
        discount = 0.0,
        trailerId = "",
        status = DefaultDropDraftStatus.CREATED,
    )
}
