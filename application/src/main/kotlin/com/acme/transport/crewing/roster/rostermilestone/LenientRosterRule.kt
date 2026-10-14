package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient roster rule is in its life cycle. */
enum class LenientRosterRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a lenient roster rule and its current status. */
data class LenientRosterRule(
    var code: String,
    var title: String,
    var revision: Int,
    var active: Boolean,
    var contactEmail: String,
    var margin: Double,
    var attributes: MutableMap<String, String>,
    var status: LenientRosterRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRosterRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRosterRuleStatus): Boolean {
        when (status) {
            LenientRosterRuleStatus.IDLE -> {
                when (next) {
                    LenientRosterRuleStatus.WARMING, LenientRosterRuleStatus.DRAINING -> return true
                }
                return false
            }
            LenientRosterRuleStatus.WARMING -> return next == LenientRosterRuleStatus.STOPPED
            LenientRosterRuleStatus.READY -> {
                when (next) {
                    LenientRosterRuleStatus.STOPPED -> return true
                }
                return false
            }
            LenientRosterRuleStatus.DRAINING -> {
                when (next) {
                    LenientRosterRuleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientRosterRuleStatus) {
        if (!canMoveTo(next)) {
            error("lenient roster rule $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($title)"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveLoad(): Double = round(revision.toDouble() * margin * 100) / 100

    fun snapshot(): LenientRosterRule {
        return LenientRosterRule(
            code = code,
            title = title,
            revision = revision,
            active = active,
            contactEmail = contactEmail,
            margin = margin,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient roster rule in status idle. */
fun newLenientRosterRule(code: String, title: String): LenientRosterRule {
    return LenientRosterRule(
        code = code,
        title = title,
        revision = 0,
        active = false,
        contactEmail = "",
        margin = 0.0,
        attributes = mutableMapOf<String, String>(),
        status = LenientRosterRuleStatus.IDLE,
    )
}
