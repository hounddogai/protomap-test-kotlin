package com.acme.transport.crewing.dutyperiod.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period template is in its life cycle. */
enum class DutyPeriodTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One duty period template, with what the planners need to act on it. */
data class DutyPeriodTemplate(
    var code: String,
    var label: String,
    var limit: Int,
    var tags: MutableList<String>,
    var endsAt: Int,
    var rate: Double,
    var dock: String,
    var status: DutyPeriodTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodTemplateStatus): Boolean {
        when (status) {
            DutyPeriodTemplateStatus.PROPOSED -> {
                when (next) {
                    DutyPeriodTemplateStatus.APPROVED, DutyPeriodTemplateStatus.WITHDRAWN -> return true
                }
                return false
            }
            DutyPeriodTemplateStatus.APPROVED -> return next == DutyPeriodTemplateStatus.REJECTED
            DutyPeriodTemplateStatus.REJECTED -> {
                when (next) {
                    DutyPeriodTemplateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodTemplateStatus) {
        if (!canMoveTo(next)) {
            error("duty period template $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
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

    fun adjustedLoad(): Double = round((limit.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): DutyPeriodTemplate {
        return DutyPeriodTemplate(
            code = code,
            label = label,
            limit = limit,
            tags = tags,
            endsAt = endsAt,
            rate = rate,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a duty period template in status proposed. */
fun newDutyPeriodTemplate(code: String, label: String): DutyPeriodTemplate {
    return DutyPeriodTemplate(
        code = code,
        label = label,
        limit = 0,
        tags = mutableListOf<String>(),
        endsAt = 0,
        rate = 0.0,
        dock = "",
        status = DutyPeriodTemplateStatus.PROPOSED,
    )
}
