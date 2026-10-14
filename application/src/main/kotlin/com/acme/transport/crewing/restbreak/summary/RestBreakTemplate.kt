package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break template is in its life cycle. */
enum class RestBreakTemplateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A rest break template as the rest break team keeps it. */
data class RestBreakTemplate(
    var code: String,
    var name: String,
    var dock: String,
    var currency: String,
    var score: Double,
    var weight: Double,
    var status: RestBreakTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakTemplateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakTemplateStatus): Boolean {
        when (status) {
            RestBreakTemplateStatus.CREATED -> {
                when (next) {
                    RestBreakTemplateStatus.VALIDATED, RestBreakTemplateStatus.PUBLISHED -> return true
                }
                return false
            }
            RestBreakTemplateStatus.VALIDATED -> {
                when (next) {
                    RestBreakTemplateStatus.PUBLISHED -> return true
                }
                return false
            }
            RestBreakTemplateStatus.PUBLISHED -> {
                when (next) {
                    RestBreakTemplateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakTemplateStatus) {
        if (!canMoveTo(next)) {
            error("rest break template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedTotal(): Double = round((weight + score) * 100) / 100

    fun snapshot(): RestBreakTemplate {
        return RestBreakTemplate(
            code = code,
            name = name,
            dock = dock,
            currency = currency,
            score = score,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rest break template in status created. */
fun newRestBreakTemplate(code: String, name: String): RestBreakTemplate {
    return RestBreakTemplate(
        code = code,
        name = name,
        dock = "",
        currency = "",
        score = 0.0,
        weight = 0.0,
        status = RestBreakTemplateStatus.CREATED,
    )
}
