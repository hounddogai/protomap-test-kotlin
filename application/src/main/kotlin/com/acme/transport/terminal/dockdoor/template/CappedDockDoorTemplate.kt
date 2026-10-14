package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped dock door template records of terminal's dock door work, and the rules, stores, and calculations
// around them.

/** Where a capped dock door template is in its life cycle. */
enum class CappedDockDoorTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One capped dock door template, with what the planners need to act on it. */
data class CappedDockDoorTemplate(
    var code: String,
    var label: String,
    var contactEmail: String,
    var archived: Boolean,
    var threshold: Double,
    var name: String,
    var quantity: Int,
    var currency: String,
    var channel: String,
    var reviewer: String,
    var status: CappedDockDoorTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDockDoorTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDockDoorTemplateStatus): Boolean {
        when (status) {
            CappedDockDoorTemplateStatus.DRAFT -> return next == CappedDockDoorTemplateStatus.PENDING || next == CappedDockDoorTemplateStatus.ACTIVE
            CappedDockDoorTemplateStatus.PENDING -> return next == CappedDockDoorTemplateStatus.CLOSED
            CappedDockDoorTemplateStatus.ACTIVE -> {
                when (next) {
                    CappedDockDoorTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            CappedDockDoorTemplateStatus.SUSPENDED -> {
                when (next) {
                    CappedDockDoorTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedDockDoorTemplateStatus) {
        if (!canMoveTo(next)) {
            error("capped dock door template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedLoad(): Double = round((threshold - quantity.toDouble()) * 100) / 100

    fun snapshot(): CappedDockDoorTemplate {
        return CappedDockDoorTemplate(
            code = code,
            label = label,
            contactEmail = contactEmail,
            archived = archived,
            threshold = threshold,
            name = name,
            quantity = quantity,
            currency = currency,
            channel = channel,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a capped dock door template in status draft. */
fun newCappedDockDoorTemplate(code: String, label: String): CappedDockDoorTemplate {
    return CappedDockDoorTemplate(
        code = code,
        label = label,
        contactEmail = "",
        archived = false,
        threshold = 0.0,
        name = "",
        quantity = 0,
        currency = "",
        channel = "",
        reviewer = "",
        status = CappedDockDoorTemplateStatus.DRAFT,
    )
}
