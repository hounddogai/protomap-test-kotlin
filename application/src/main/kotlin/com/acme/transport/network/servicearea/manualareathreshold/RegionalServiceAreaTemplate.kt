package com.acme.transport.network.servicearea.manualareathreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional service area template is in its life cycle. */
enum class RegionalServiceAreaTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A regional service area template as the service area team keeps it. */
data class RegionalServiceAreaTemplate(
    var code: String,
    var label: String,
    var discount: Double,
    var country: String,
    var batchSize: Int,
    var dock: String,
    var minimum: Double,
    var status: RegionalServiceAreaTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalServiceAreaTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalServiceAreaTemplateStatus): Boolean {
        when (status) {
            RegionalServiceAreaTemplateStatus.PLANNED -> {
                when (next) {
                    RegionalServiceAreaTemplateStatus.COMPLETED, RegionalServiceAreaTemplateStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalServiceAreaTemplateStatus.SCHEDULED -> return next == RegionalServiceAreaTemplateStatus.CANCELLED
            RegionalServiceAreaTemplateStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalServiceAreaTemplateStatus.COMPLETED -> return true
                }
                return false
            }
            RegionalServiceAreaTemplateStatus.COMPLETED -> {
                when (next) {
                    RegionalServiceAreaTemplateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalServiceAreaTemplateStatus) {
        if (!canMoveTo(next)) {
            error("regional service area template $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveVolume(): Double = round((minimum - discount) * 100) / 100

    fun snapshot(): RegionalServiceAreaTemplate {
        return RegionalServiceAreaTemplate(
            code = code,
            label = label,
            discount = discount,
            country = country,
            batchSize = batchSize,
            dock = dock,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a regional service area template in status planned. */
fun newRegionalServiceAreaTemplate(code: String, label: String): RegionalServiceAreaTemplate {
    return RegionalServiceAreaTemplate(
        code = code,
        label = label,
        discount = 0.0,
        country = "",
        batchSize = 0,
        dock = "",
        minimum = 0.0,
        status = RegionalServiceAreaTemplateStatus.PLANNED,
    )
}
