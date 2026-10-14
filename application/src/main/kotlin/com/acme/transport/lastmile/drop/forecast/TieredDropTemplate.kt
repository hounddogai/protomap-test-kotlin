package com.acme.transport.lastmile.drop.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered drop template is in its life cycle. */
enum class TieredDropTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a tiered drop template and its current status. */
data class TieredDropTemplate(
    var code: String,
    var name: String,
    var approved: Boolean,
    var locale: String,
    var updatedAt: Int,
    var title: String,
    var archived: Boolean,
    var revision: Int,
    var tags: MutableList<String>,
    var region: String,
    var status: TieredDropTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredDropTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredDropTemplateStatus): Boolean {
        when (status) {
            TieredDropTemplateStatus.CREATED -> return next == TieredDropTemplateStatus.VALIDATED || next == TieredDropTemplateStatus.RETIRED
            TieredDropTemplateStatus.VALIDATED -> return next == TieredDropTemplateStatus.PUBLISHED || next == TieredDropTemplateStatus.RETIRED
            TieredDropTemplateStatus.PUBLISHED -> return next == TieredDropTemplateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredDropTemplateStatus) {
        if (!canMoveTo(next)) {
            error("tiered drop template $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round(updatedAt.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): TieredDropTemplate {
        return TieredDropTemplate(
            code = code,
            name = name,
            approved = approved,
            locale = locale,
            updatedAt = updatedAt,
            title = title,
            archived = archived,
            revision = revision,
            tags = tags,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tiered drop template in status created. */
fun newTieredDropTemplate(code: String, name: String): TieredDropTemplate {
    return TieredDropTemplate(
        code = code,
        name = name,
        approved = false,
        locale = "",
        updatedAt = 0,
        title = "",
        archived = false,
        revision = 0,
        tags = mutableListOf<String>(),
        region = "",
        status = TieredDropTemplateStatus.CREATED,
    )
}
