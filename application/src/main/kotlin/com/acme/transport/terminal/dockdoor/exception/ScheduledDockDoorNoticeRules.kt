package com.acme.transport.terminal.dockdoor.exception

import com.acme.transport.kit.text.normalize

/** A rule a scheduled dock door notice must pass. Check returns its problems, or none. */
interface ScheduledDockDoorNoticeRule {
    fun check(item: ScheduledDockDoorNotice): MutableList<String>
}

data class ScheduledDockDoorNoticeAllowedRegionRule(
    var allowed: MutableList<String>,
) : ScheduledDockDoorNoticeRule {
    override fun check(item: ScheduledDockDoorNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorNoticePinnedGuardRule(var limit: Int) : ScheduledDockDoorNoticeRule {
    override fun check(item: ScheduledDockDoorNotice): MutableList<String> {
        if (item.pinned && item.pallets > limit) {
            return mutableListOf<String>("pallets ${item.pallets} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorNoticeRequiredTitleRule(var message: String) : ScheduledDockDoorNoticeRule {
    override fun check(item: ScheduledDockDoorNotice): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDockDoorNoticeMaximumRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledDockDoorNoticeRule {
    override fun check(item: ScheduledDockDoorNotice): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class ScheduledDockDoorNoticeRequiredLocaleRule(var message: String) : ScheduledDockDoorNoticeRule {
    override fun check(item: ScheduledDockDoorNotice): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled dock door notice. */
data class ScheduledDockDoorNoticeValidator(var rules: MutableList<ScheduledDockDoorNoticeRule>) {
    fun validate(item: ScheduledDockDoorNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            val found = rule.check(item)
            if (found.size == 0) {
                continue
            }
            for (problem in found) {
                problems.add("${item.code}: $problem")
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledDockDoorNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDockDoorNotice>): MutableMap<String, MutableList<String>> {
        val report = mutableMapOf<String, MutableList<String>>()
        for (item in items) {
            val problems = validate(item)
            if (problems.size > 0) {
                report[item.code] = problems
            }
        }
        return report
    }
}

fun defaultScheduledDockDoorNoticeValidator(): ScheduledDockDoorNoticeValidator {
    return ScheduledDockDoorNoticeValidator(
        mutableListOf<ScheduledDockDoorNoticeRule>(
            ScheduledDockDoorNoticeAllowedRegionRule(mutableListOf<String>("batched", "rolling", "strict")),
            ScheduledDockDoorNoticePinnedGuardRule(120),
            ScheduledDockDoorNoticeRequiredTitleRule("scheduled dock door notice needs an entry"),
            ScheduledDockDoorNoticeMaximumRangeRule(5.3, 2451.7),
            ScheduledDockDoorNoticeRequiredLocaleRule("scheduled dock door notice needs an entry"),
        ),
    )
}
