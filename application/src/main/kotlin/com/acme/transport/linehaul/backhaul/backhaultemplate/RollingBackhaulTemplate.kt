package com.acme.transport.linehaul.backhaul.backhaultemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling backhaul template records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a rolling backhaul template is in its life cycle. */
enum class RollingBackhaulTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rolling backhaul template. Its code is unique within a tenant. */
data class RollingBackhaulTemplate(
    var code: String,
    var name: String,
    var timezone: String,
    var score: Double,
    var capacity: Int,
    var locale: String,
    var status: RollingBackhaulTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingBackhaulTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingBackhaulTemplateStatus): Boolean {
        when (status) {
            RollingBackhaulTemplateStatus.IDLE -> {
                when (next) {
                    RollingBackhaulTemplateStatus.WARMING -> return true
                }
                return false
            }
            RollingBackhaulTemplateStatus.WARMING -> return next == RollingBackhaulTemplateStatus.READY
            RollingBackhaulTemplateStatus.READY -> {
                when (next) {
                    RollingBackhaulTemplateStatus.DRAINING -> return true
                }
                return false
            }
            RollingBackhaulTemplateStatus.DRAINING -> return next == RollingBackhaulTemplateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RollingBackhaulTemplateStatus) {
        if (!canMoveTo(next)) {
            error("rolling backhaul template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveVolume(): Double = round((capacity.toDouble() + score) * 100) / 100

    fun snapshot(): RollingBackhaulTemplate {
        return RollingBackhaulTemplate(
            code = code,
            name = name,
            timezone = timezone,
            score = score,
            capacity = capacity,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a rolling backhaul template in status idle. */
fun newRollingBackhaulTemplate(code: String, name: String): RollingBackhaulTemplate {
    return RollingBackhaulTemplate(
        code = code,
        name = name,
        timezone = "",
        score = 0.0,
        capacity = 0,
        locale = "",
        status = RollingBackhaulTemplateStatus.IDLE,
    )
}
