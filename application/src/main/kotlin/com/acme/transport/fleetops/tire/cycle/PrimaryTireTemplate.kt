package com.acme.transport.fleetops.tire.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary tire template is in its life cycle. */
enum class PrimaryTireTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary tire template. Its code is unique within a tenant. */
data class PrimaryTireTemplate(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var title: String,
    var score: Double,
    var category: String,
    var locale: String,
    var status: PrimaryTireTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTireTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTireTemplateStatus): Boolean {
        when (status) {
            PrimaryTireTemplateStatus.IDLE -> {
                when (next) {
                    PrimaryTireTemplateStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryTireTemplateStatus.WARMING -> return next == PrimaryTireTemplateStatus.STOPPED
            PrimaryTireTemplateStatus.READY -> return next == PrimaryTireTemplateStatus.DRAINING || next == PrimaryTireTemplateStatus.STOPPED
            PrimaryTireTemplateStatus.DRAINING -> return next == PrimaryTireTemplateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PrimaryTireTemplateStatus) {
        if (!canMoveTo(next)) {
            error("primary tire template $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"primary tire template"} $code"

    fun netValue(): Double = round((score - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): PrimaryTireTemplate {
        return PrimaryTireTemplate(
            code = code,
            label = label,
            updatedAt = updatedAt,
            title = title,
            score = score,
            category = category,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a primary tire template in status idle. */
fun newPrimaryTireTemplate(code: String, label: String): PrimaryTireTemplate {
    return PrimaryTireTemplate(
        code = code,
        label = label,
        updatedAt = 0,
        title = "",
        score = 0.0,
        category = "",
        locale = "",
        status = PrimaryTireTemplateStatus.IDLE,
    )
}
