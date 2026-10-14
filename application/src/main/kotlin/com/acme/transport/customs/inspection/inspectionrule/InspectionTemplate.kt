package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection template is in its life cycle. */
enum class InspectionTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A inspection template as the inspection team keeps it. */
data class InspectionTemplate(
    var code: String,
    var name: String,
    var country: String,
    var dock: String,
    var capacity: Int,
    var reviewer: String,
    var retries: Int,
    var expiresAt: Int,
    var status: InspectionTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionTemplateStatus): Boolean {
        when (status) {
            InspectionTemplateStatus.CREATED -> {
                when (next) {
                    InspectionTemplateStatus.PUBLISHED -> return true
                }
                return false
            }
            InspectionTemplateStatus.VALIDATED -> {
                when (next) {
                    InspectionTemplateStatus.RETIRED -> return true
                }
                return false
            }
            InspectionTemplateStatus.PUBLISHED -> {
                when (next) {
                    InspectionTemplateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionTemplateStatus) {
        if (!canMoveTo(next)) {
            error("inspection template $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"inspection template"} $code"

    fun projectedValue(): Double = round(capacity.toDouble() * retries.toDouble() * 100) / 100

    fun snapshot(): InspectionTemplate {
        return InspectionTemplate(
            code = code,
            name = name,
            country = country,
            dock = dock,
            capacity = capacity,
            reviewer = reviewer,
            retries = retries,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a inspection template in status created. */
fun newInspectionTemplate(code: String, name: String): InspectionTemplate {
    return InspectionTemplate(
        code = code,
        name = name,
        country = "",
        dock = "",
        capacity = 0,
        reviewer = "",
        retries = 0,
        expiresAt = 0,
        status = InspectionTemplateStatus.CREATED,
    )
}
