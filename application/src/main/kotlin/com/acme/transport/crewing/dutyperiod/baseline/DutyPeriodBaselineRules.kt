package com.acme.transport.crewing.dutyperiod.baseline

import com.acme.transport.kit.text.normalize

/** A rule a duty period baseline must pass. Check returns its problems, or none. */
interface DutyPeriodBaselineRule {
    fun check(item: DutyPeriodBaseline): MutableList<String>
}

data class DutyPeriodBaselineRequiredCodeRule(var message: String) : DutyPeriodBaselineRule {
    override fun check(item: DutyPeriodBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodBaselineRequiredSegmentRule(var message: String) : DutyPeriodBaselineRule {
    override fun check(item: DutyPeriodBaseline): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodBaselineExternalRefLengthRule(var maxLength: Int) : DutyPeriodBaselineRule {
    override fun check(item: DutyPeriodBaseline): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodBaselineAllowedLocaleRule(var allowed: MutableList<String>) : DutyPeriodBaselineRule {
    override fun check(item: DutyPeriodBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodBaselineActiveGuardRule(var limit: Int) : DutyPeriodBaselineRule {
    override fun check(item: DutyPeriodBaseline): MutableList<String> {
        if (item.active && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodBaselineSegmentLengthRule(var maxLength: Int) : DutyPeriodBaselineRule {
    override fun check(item: DutyPeriodBaseline): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period baseline. */
data class DutyPeriodBaselineValidator(var rules: MutableList<DutyPeriodBaselineRule>) {
    fun validate(item: DutyPeriodBaseline): MutableList<String> {
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

    fun requireValid(item: DutyPeriodBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodBaselineValidator(): DutyPeriodBaselineValidator {
    return DutyPeriodBaselineValidator(
        mutableListOf<DutyPeriodBaselineRule>(
            DutyPeriodBaselineRequiredCodeRule("duty period baseline needs a value"),
            DutyPeriodBaselineRequiredSegmentRule("duty period baseline needs a value"),
            DutyPeriodBaselineExternalRefLengthRule(64),
            DutyPeriodBaselineAllowedLocaleRule(mutableListOf<String>("weighted", "weighted", "fallback")),
            DutyPeriodBaselineActiveGuardRule(255),
            DutyPeriodBaselineSegmentLengthRule(64),
        ),
    )
}
