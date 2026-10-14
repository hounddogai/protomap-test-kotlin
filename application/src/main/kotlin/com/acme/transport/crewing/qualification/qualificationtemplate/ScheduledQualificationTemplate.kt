package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled qualification template records of crewing's qualification work, and the rules, stores, and
// calculations around them.

/** Where a scheduled qualification template is in its life cycle. */
enum class ScheduledQualificationTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scheduled qualification template. Its code is unique within a tenant. */
data class ScheduledQualificationTemplate(
    var code: String,
    var label: String,
    var version: Int,
    var currency: String,
    var pallets: Int,
    var phone: String,
    var tags: MutableList<String>,
    var status: ScheduledQualificationTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledQualificationTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledQualificationTemplateStatus): Boolean {
        when (status) {
            ScheduledQualificationTemplateStatus.IDLE -> {
                when (next) {
                    ScheduledQualificationTemplateStatus.DRAINING, ScheduledQualificationTemplateStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledQualificationTemplateStatus.WARMING -> return next == ScheduledQualificationTemplateStatus.STOPPED
            ScheduledQualificationTemplateStatus.READY -> return next == ScheduledQualificationTemplateStatus.DRAINING || next == ScheduledQualificationTemplateStatus.STOPPED
            ScheduledQualificationTemplateStatus.DRAINING -> {
                when (next) {
                    ScheduledQualificationTemplateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledQualificationTemplateStatus) {
        if (!canMoveTo(next)) {
            error("scheduled qualification template $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"scheduled qualification template"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveTotal(): Double = round((version.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): ScheduledQualificationTemplate {
        return ScheduledQualificationTemplate(
            code = code,
            label = label,
            version = version,
            currency = currency,
            pallets = pallets,
            phone = phone,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled qualification template in status idle. */
fun newScheduledQualificationTemplate(code: String, label: String): ScheduledQualificationTemplate {
    return ScheduledQualificationTemplate(
        code = code,
        label = label,
        version = 0,
        currency = "",
        pallets = 0,
        phone = "",
        tags = mutableListOf<String>(),
        status = ScheduledQualificationTemplateStatus.IDLE,
    )
}
