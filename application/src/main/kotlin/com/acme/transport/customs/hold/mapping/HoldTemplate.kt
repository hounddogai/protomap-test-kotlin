package com.acme.transport.customs.hold.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hold template is in its life cycle. */
enum class HoldTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A hold template. Its code is unique within a tenant. */
data class HoldTemplate(
    var code: String,
    var name: String,
    var contactEmail: String,
    var limit: Int,
    var routeCode: String,
    var carrier: String,
    var maximum: Double,
    var discount: Double,
    var status: HoldTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldTemplateStatus): Boolean {
        when (status) {
            HoldTemplateStatus.IDLE -> {
                when (next) {
                    HoldTemplateStatus.DRAINING -> return true
                }
                return false
            }
            HoldTemplateStatus.WARMING -> {
                when (next) {
                    HoldTemplateStatus.READY, HoldTemplateStatus.DRAINING -> return true
                }
                return false
            }
            HoldTemplateStatus.READY -> {
                when (next) {
                    HoldTemplateStatus.DRAINING, HoldTemplateStatus.STOPPED -> return true
                }
                return false
            }
            HoldTemplateStatus.DRAINING -> {
                when (next) {
                    HoldTemplateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HoldTemplateStatus) {
        if (!canMoveTo(next)) {
            error("hold template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveVolume(): Double = round((limit.toDouble() + maximum) * 100) / 100

    fun snapshot(): HoldTemplate {
        return HoldTemplate(
            code = code,
            name = name,
            contactEmail = contactEmail,
            limit = limit,
            routeCode = routeCode,
            carrier = carrier,
            maximum = maximum,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hold template in status idle. */
fun newHoldTemplate(code: String, name: String): HoldTemplate {
    return HoldTemplate(
        code = code,
        name = name,
        contactEmail = "",
        limit = 0,
        routeCode = "",
        carrier = "",
        maximum = 0.0,
        discount = 0.0,
        status = HoldTemplateStatus.IDLE,
    )
}
