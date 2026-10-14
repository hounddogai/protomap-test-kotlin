package com.acme.transport.emissions.evcharge.template

import com.acme.transport.kit.text.normalize

// The default ev charge template records of emissions's ev charge work, and the rules, stores, and
// calculations around them.

/** Where a default ev charge template is in its life cycle. */
enum class DefaultEvChargeTemplateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A default ev charge template. Its code is unique within a tenant. */
data class DefaultEvChargeTemplate(
    var code: String,
    var title: String,
    var score: Double,
    var notes: String,
    var region: String,
    var segment: String,
    var attributes: MutableMap<String, String>,
    var dock: String,
    var shiftCode: String,
    var status: DefaultEvChargeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultEvChargeTemplateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultEvChargeTemplateStatus): Boolean {
        when (status) {
            DefaultEvChargeTemplateStatus.IDLE -> {
                when (next) {
                    DefaultEvChargeTemplateStatus.READY, DefaultEvChargeTemplateStatus.STOPPED -> return true
                }
                return false
            }
            DefaultEvChargeTemplateStatus.WARMING -> {
                when (next) {
                    DefaultEvChargeTemplateStatus.READY -> return true
                }
                return false
            }
            DefaultEvChargeTemplateStatus.READY -> return next == DefaultEvChargeTemplateStatus.DRAINING || next == DefaultEvChargeTemplateStatus.STOPPED
            DefaultEvChargeTemplateStatus.DRAINING -> {
                when (next) {
                    DefaultEvChargeTemplateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultEvChargeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("default ev charge template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = score

    fun snapshot(): DefaultEvChargeTemplate {
        return DefaultEvChargeTemplate(
            code = code,
            title = title,
            score = score,
            notes = notes,
            region = region,
            segment = segment,
            attributes = attributes,
            dock = dock,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a default ev charge template in status idle. */
fun newDefaultEvChargeTemplate(code: String, title: String): DefaultEvChargeTemplate {
    return DefaultEvChargeTemplate(
        code = code,
        title = title,
        score = 0.0,
        notes = "",
        region = "",
        segment = "",
        attributes = mutableMapOf<String, String>(),
        dock = "",
        shiftCode = "",
        status = DefaultEvChargeTemplateStatus.IDLE,
    )
}
