package com.acme.transport.terminal.dwell.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled dwell template is in its life cycle. */
enum class ScheduledDwellTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One scheduled dwell template, with what the planners need to act on it. */
data class ScheduledDwellTemplate(
    var code: String,
    var title: String,
    var checksum: String,
    var tags: MutableList<String>,
    var shiftCode: String,
    var fuelLiters: Double,
    var loadKg: Double,
    var currency: String,
    var budget: Double,
    var contactEmail: String,
    var status: ScheduledDwellTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDwellTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDwellTemplateStatus): Boolean {
        when (status) {
            ScheduledDwellTemplateStatus.IDLE -> return next == ScheduledDwellTemplateStatus.READY || next == ScheduledDwellTemplateStatus.DRAINING
            ScheduledDwellTemplateStatus.WARMING -> {
                when (next) {
                    ScheduledDwellTemplateStatus.READY -> return true
                }
                return false
            }
            ScheduledDwellTemplateStatus.READY -> {
                when (next) {
                    ScheduledDwellTemplateStatus.DRAINING, ScheduledDwellTemplateStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledDwellTemplateStatus.DRAINING -> return next == ScheduledDwellTemplateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledDwellTemplateStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dwell template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round((budget + loadKg) * 100) / 100

    fun snapshot(): ScheduledDwellTemplate {
        return ScheduledDwellTemplate(
            code = code,
            title = title,
            checksum = checksum,
            tags = tags,
            shiftCode = shiftCode,
            fuelLiters = fuelLiters,
            loadKg = loadKg,
            currency = currency,
            budget = budget,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled dwell template in status idle. */
fun newScheduledDwellTemplate(code: String, title: String): ScheduledDwellTemplate {
    return ScheduledDwellTemplate(
        code = code,
        title = title,
        checksum = "",
        tags = mutableListOf<String>(),
        shiftCode = "",
        fuelLiters = 0.0,
        loadKg = 0.0,
        currency = "",
        budget = 0.0,
        contactEmail = "",
        status = ScheduledDwellTemplateStatus.IDLE,
    )
}
