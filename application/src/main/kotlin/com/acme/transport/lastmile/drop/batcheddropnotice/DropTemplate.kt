package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a drop template is in its life cycle. */
enum class DropTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A drop template as the drop team keeps it. */
data class DropTemplate(
    var code: String,
    var label: String,
    var contactEmail: String,
    var maximum: Double,
    var notes: String,
    var weight: Double,
    var channel: String,
    var tags: MutableList<String>,
    var shiftCode: String,
    var archived: Boolean,
    var margin: Double,
    var status: DropTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DropTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DropTemplateStatus): Boolean {
        when (status) {
            DropTemplateStatus.IDLE -> return next == DropTemplateStatus.WARMING || next == DropTemplateStatus.STOPPED
            DropTemplateStatus.WARMING -> {
                when (next) {
                    DropTemplateStatus.DRAINING, DropTemplateStatus.STOPPED -> return true
                }
                return false
            }
            DropTemplateStatus.READY -> {
                when (next) {
                    DropTemplateStatus.STOPPED -> return true
                }
                return false
            }
            DropTemplateStatus.DRAINING -> {
                when (next) {
                    DropTemplateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DropTemplateStatus) {
        if (!canMoveTo(next)) {
            error("drop template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round((maximum + margin) * 100) / 100

    fun snapshot(): DropTemplate {
        return DropTemplate(
            code = code,
            label = label,
            contactEmail = contactEmail,
            maximum = maximum,
            notes = notes,
            weight = weight,
            channel = channel,
            tags = tags,
            shiftCode = shiftCode,
            archived = archived,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a drop template in status idle. */
fun newDropTemplate(code: String, label: String): DropTemplate {
    return DropTemplate(
        code = code,
        label = label,
        contactEmail = "",
        maximum = 0.0,
        notes = "",
        weight = 0.0,
        channel = "",
        tags = mutableListOf<String>(),
        shiftCode = "",
        archived = false,
        margin = 0.0,
        status = DropTemplateStatus.IDLE,
    )
}
