package com.acme.transport.customs.declaration.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped declaration template is in its life cycle. */
enum class CappedDeclarationTemplateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One capped declaration template, with what the planners need to act on it. */
data class CappedDeclarationTemplate(
    var code: String,
    var title: String,
    var limit: Int,
    var ownerId: String,
    var pallets: Int,
    var priority: Int,
    var reviewer: String,
    var status: CappedDeclarationTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDeclarationTemplateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDeclarationTemplateStatus): Boolean {
        when (status) {
            CappedDeclarationTemplateStatus.OPEN -> {
                when (next) {
                    CappedDeclarationTemplateStatus.RESOLVED -> return true
                }
                return false
            }
            CappedDeclarationTemplateStatus.IN_REVIEW -> return next == CappedDeclarationTemplateStatus.RESOLVED || next == CappedDeclarationTemplateStatus.REOPENED
            CappedDeclarationTemplateStatus.RESOLVED -> {
                when (next) {
                    CappedDeclarationTemplateStatus.REOPENED -> return true
                }
                return false
            }
            CappedDeclarationTemplateStatus.REOPENED -> return next == CappedDeclarationTemplateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CappedDeclarationTemplateStatus) {
        if (!canMoveTo(next)) {
            error("capped declaration template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped declaration template"} $code"

    fun netTotal(): Double = round(priority.toDouble() * pallets.toDouble() * 100) / 100

    fun snapshot(): CappedDeclarationTemplate {
        return CappedDeclarationTemplate(
            code = code,
            title = title,
            limit = limit,
            ownerId = ownerId,
            pallets = pallets,
            priority = priority,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped declaration template in status open. */
fun newCappedDeclarationTemplate(code: String, title: String): CappedDeclarationTemplate {
    return CappedDeclarationTemplate(
        code = code,
        title = title,
        limit = 0,
        ownerId = "",
        pallets = 0,
        priority = 0,
        reviewer = "",
        status = CappedDeclarationTemplateStatus.OPEN,
    )
}
