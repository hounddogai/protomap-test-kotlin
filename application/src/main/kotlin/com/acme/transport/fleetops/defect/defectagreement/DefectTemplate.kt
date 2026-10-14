package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect template is in its life cycle. */
enum class DefectTemplateStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A defect template. Its code is unique within a tenant. */
data class DefectTemplate(
    var code: String,
    var name: String,
    var dock: String,
    var label: String,
    var startsAt: Int,
    var ceiling: Double,
    var summary: String,
    var attempts: Int,
    var capacity: Int,
    var status: DefectTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectTemplateStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectTemplateStatus): Boolean {
        when (status) {
            DefectTemplateStatus.PROPOSED -> {
                when (next) {
                    DefectTemplateStatus.REJECTED, DefectTemplateStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefectTemplateStatus.APPROVED -> {
                when (next) {
                    DefectTemplateStatus.REJECTED, DefectTemplateStatus.WITHDRAWN -> return true
                }
                return false
            }
            DefectTemplateStatus.REJECTED -> {
                when (next) {
                    DefectTemplateStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectTemplateStatus) {
        if (!canMoveTo(next)) {
            error("defect template $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun adjustedVolume(): Double = round(attempts.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): DefectTemplate {
        return DefectTemplate(
            code = code,
            name = name,
            dock = dock,
            label = label,
            startsAt = startsAt,
            ceiling = ceiling,
            summary = summary,
            attempts = attempts,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a defect template in status proposed. */
fun newDefectTemplate(code: String, name: String): DefectTemplate {
    return DefectTemplate(
        code = code,
        name = name,
        dock = "",
        label = "",
        startsAt = 0,
        ceiling = 0.0,
        summary = "",
        attempts = 0,
        capacity = 0,
        status = DefectTemplateStatus.PROPOSED,
    )
}
