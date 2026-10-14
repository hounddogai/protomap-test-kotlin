package com.acme.transport.linehaul.tender.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped tender template is in its life cycle. */
enum class CappedTenderTemplateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a capped tender template and its current status. */
data class CappedTenderTemplate(
    var code: String,
    var label: String,
    var version: Int,
    var ownerId: String,
    var category: String,
    var dock: String,
    var routeCode: String,
    var minimum: Double,
    var status: CappedTenderTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedTenderTemplateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedTenderTemplateStatus): Boolean {
        when (status) {
            CappedTenderTemplateStatus.OPEN -> {
                when (next) {
                    CappedTenderTemplateStatus.RESOLVED -> return true
                }
                return false
            }
            CappedTenderTemplateStatus.IN_REVIEW -> return next == CappedTenderTemplateStatus.RESOLVED || next == CappedTenderTemplateStatus.REOPENED
            CappedTenderTemplateStatus.RESOLVED -> return next == CappedTenderTemplateStatus.ARCHIVED
            CappedTenderTemplateStatus.REOPENED -> return next == CappedTenderTemplateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CappedTenderTemplateStatus) {
        if (!canMoveTo(next)) {
            error("capped tender template $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round(version.toDouble() * minimum * 100) / 100

    fun snapshot(): CappedTenderTemplate {
        return CappedTenderTemplate(
            code = code,
            label = label,
            version = version,
            ownerId = ownerId,
            category = category,
            dock = dock,
            routeCode = routeCode,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped tender template in status open. */
fun newCappedTenderTemplate(code: String, label: String): CappedTenderTemplate {
    return CappedTenderTemplate(
        code = code,
        label = label,
        version = 0,
        ownerId = "",
        category = "",
        dock = "",
        routeCode = "",
        minimum = 0.0,
        status = CappedTenderTemplateStatus.OPEN,
    )
}
