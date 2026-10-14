package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery template records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a redelivery template is in its life cycle. */
enum class RedeliveryTemplateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a redelivery template and its current status. */
data class RedeliveryTemplate(
    var code: String,
    var title: String,
    var ratio: Double,
    var attributes: MutableMap<String, String>,
    var expiresAt: Int,
    var floorValue: Double,
    var quantity: Int,
    var loadKg: Double,
    var windowDays: Int,
    var status: RedeliveryTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryTemplateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryTemplateStatus): Boolean {
        when (status) {
            RedeliveryTemplateStatus.OPEN -> return next == RedeliveryTemplateStatus.RESOLVED
            RedeliveryTemplateStatus.IN_REVIEW -> return next == RedeliveryTemplateStatus.REOPENED
            RedeliveryTemplateStatus.RESOLVED -> {
                when (next) {
                    RedeliveryTemplateStatus.REOPENED, RedeliveryTemplateStatus.ARCHIVED -> return true
                }
                return false
            }
            RedeliveryTemplateStatus.REOPENED -> return next == RedeliveryTemplateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RedeliveryTemplateStatus) {
        if (!canMoveTo(next)) {
            error("redelivery template $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveExposure(): Double = round((expiresAt.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryTemplate {
        return RedeliveryTemplate(
            code = code,
            title = title,
            ratio = ratio,
            attributes = attributes,
            expiresAt = expiresAt,
            floorValue = floorValue,
            quantity = quantity,
            loadKg = loadKg,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a redelivery template in status open. */
fun newRedeliveryTemplate(code: String, title: String): RedeliveryTemplate {
    return RedeliveryTemplate(
        code = code,
        title = title,
        ratio = 0.0,
        attributes = mutableMapOf<String, String>(),
        expiresAt = 0,
        floorValue = 0.0,
        quantity = 0,
        loadKg = 0.0,
        windowDays = 0,
        status = RedeliveryTemplateStatus.OPEN,
    )
}
