package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global trailer yard template is in its life cycle. */
enum class GlobalTrailerYardTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A global trailer yard template as the trailer yard team keeps it. */
data class GlobalTrailerYardTemplate(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var retries: Int,
    var revision: Int,
    var attempts: Int,
    var contactEmail: String,
    var status: GlobalTrailerYardTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTrailerYardTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTrailerYardTemplateStatus): Boolean {
        when (status) {
            GlobalTrailerYardTemplateStatus.CREATED -> {
                when (next) {
                    GlobalTrailerYardTemplateStatus.VALIDATED -> return true
                }
                return false
            }
            GlobalTrailerYardTemplateStatus.VALIDATED -> {
                when (next) {
                    GlobalTrailerYardTemplateStatus.PUBLISHED, GlobalTrailerYardTemplateStatus.RETIRED -> return true
                }
                return false
            }
            GlobalTrailerYardTemplateStatus.PUBLISHED -> return next == GlobalTrailerYardTemplateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: GlobalTrailerYardTemplateStatus) {
        if (!canMoveTo(next)) {
            error("global trailer yard template $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round((attempts.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): GlobalTrailerYardTemplate {
        return GlobalTrailerYardTemplate(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            retries = retries,
            revision = revision,
            attempts = attempts,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global trailer yard template in status created. */
fun newGlobalTrailerYardTemplate(code: String, title: String): GlobalTrailerYardTemplate {
    return GlobalTrailerYardTemplate(
        code = code,
        title = title,
        etaMinutes = 0,
        retries = 0,
        revision = 0,
        attempts = 0,
        contactEmail = "",
        status = GlobalTrailerYardTemplateStatus.CREATED,
    )
}
