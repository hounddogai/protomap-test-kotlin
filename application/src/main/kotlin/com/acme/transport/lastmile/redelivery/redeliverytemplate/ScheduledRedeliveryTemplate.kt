package com.acme.transport.lastmile.redelivery.redeliverytemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled redelivery template records of lastmile's redelivery work, and the rules, stores, and
// calculations around them.

/** Where a scheduled redelivery template is in its life cycle. */
enum class ScheduledRedeliveryTemplateStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One scheduled redelivery template, with what the planners need to act on it. */
data class ScheduledRedeliveryTemplate(
    var code: String,
    var name: String,
    var contactEmail: String,
    var active: Boolean,
    var source: String,
    var cost: Double,
    var tags: MutableList<String>,
    var attempts: Int,
    var reviewer: String,
    var category: String,
    var attributes: MutableMap<String, String>,
    var status: ScheduledRedeliveryTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRedeliveryTemplateStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRedeliveryTemplateStatus): Boolean {
        when (status) {
            ScheduledRedeliveryTemplateStatus.NEW -> return next == ScheduledRedeliveryTemplateStatus.QUEUED
            ScheduledRedeliveryTemplateStatus.QUEUED -> return next == ScheduledRedeliveryTemplateStatus.DONE || next == ScheduledRedeliveryTemplateStatus.FAILED
            ScheduledRedeliveryTemplateStatus.RUNNING -> return next == ScheduledRedeliveryTemplateStatus.DONE || next == ScheduledRedeliveryTemplateStatus.FAILED
            ScheduledRedeliveryTemplateStatus.DONE -> return next == ScheduledRedeliveryTemplateStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledRedeliveryTemplateStatus) {
        if (!canMoveTo(next)) {
            error("scheduled redelivery template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled redelivery template"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round(cost * attempts.toDouble() * 100) / 100

    fun snapshot(): ScheduledRedeliveryTemplate {
        return ScheduledRedeliveryTemplate(
            code = code,
            name = name,
            contactEmail = contactEmail,
            active = active,
            source = source,
            cost = cost,
            tags = tags,
            attempts = attempts,
            reviewer = reviewer,
            category = category,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a scheduled redelivery template in status new. */
fun newScheduledRedeliveryTemplate(code: String, name: String): ScheduledRedeliveryTemplate {
    return ScheduledRedeliveryTemplate(
        code = code,
        name = name,
        contactEmail = "",
        active = false,
        source = "",
        cost = 0.0,
        tags = mutableListOf<String>(),
        attempts = 0,
        reviewer = "",
        category = "",
        attributes = mutableMapOf<String, String>(),
        status = ScheduledRedeliveryTemplateStatus.NEW,
    )
}
