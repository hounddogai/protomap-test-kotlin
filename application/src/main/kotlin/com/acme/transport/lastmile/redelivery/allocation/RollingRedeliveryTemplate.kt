package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling redelivery template is in its life cycle. */
enum class RollingRedeliveryTemplateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a rolling redelivery template and its current status. */
data class RollingRedeliveryTemplate(
    var code: String,
    var label: String,
    var lane: String,
    var rate: Double,
    var ratio: Double,
    var pallets: Int,
    var approved: Boolean,
    var status: RollingRedeliveryTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRedeliveryTemplateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRedeliveryTemplateStatus): Boolean {
        when (status) {
            RollingRedeliveryTemplateStatus.DRAFT -> {
                when (next) {
                    RollingRedeliveryTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            RollingRedeliveryTemplateStatus.PENDING -> {
                when (next) {
                    RollingRedeliveryTemplateStatus.ACTIVE, RollingRedeliveryTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            RollingRedeliveryTemplateStatus.ACTIVE -> {
                when (next) {
                    RollingRedeliveryTemplateStatus.SUSPENDED -> return true
                }
                return false
            }
            RollingRedeliveryTemplateStatus.SUSPENDED -> {
                when (next) {
                    RollingRedeliveryTemplateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingRedeliveryTemplateStatus) {
        if (!canMoveTo(next)) {
            error("rolling redelivery template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveVolume(): Double = round((pallets.toDouble() + rate) * 100) / 100

    fun snapshot(): RollingRedeliveryTemplate {
        return RollingRedeliveryTemplate(
            code = code,
            label = label,
            lane = lane,
            rate = rate,
            ratio = ratio,
            pallets = pallets,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling redelivery template in status draft. */
fun newRollingRedeliveryTemplate(code: String, label: String): RollingRedeliveryTemplate {
    return RollingRedeliveryTemplate(
        code = code,
        label = label,
        lane = "",
        rate = 0.0,
        ratio = 0.0,
        pallets = 0,
        approved = false,
        status = RollingRedeliveryTemplateStatus.DRAFT,
    )
}
