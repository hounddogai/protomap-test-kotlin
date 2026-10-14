package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.text.normalize

/** A rule a locker summary must pass. Check returns its problems, or none. */
interface LockerSummaryRule {
    fun check(item: LockerSummary): MutableList<String>
}

data class LockerSummaryPriorityRangeRule(var min: Int, var max: Int) : LockerSummaryRule {
    override fun check(item: LockerSummary): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerSummaryRequiredTimezoneRule(var message: String) : LockerSummaryRule {
    override fun check(item: LockerSummary): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerSummaryRequiredNameRule(var message: String) : LockerSummaryRule {
    override fun check(item: LockerSummary): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerSummaryAllowedTimezoneRule(var allowed: MutableList<String>) : LockerSummaryRule {
    override fun check(item: LockerSummary): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is missing")
        }
        return mutableListOf<String>()
    }
}

data class LockerSummaryRouteCodeLengthRule(var maxLength: Int) : LockerSummaryRule {
    override fun check(item: LockerSummary): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerSummaryActiveGuardRule(var limit: Double) : LockerSummaryRule {
    override fun check(item: LockerSummary): MutableList<String> {
        if (item.active && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker summary. */
data class LockerSummaryValidator(var rules: MutableList<LockerSummaryRule>) {
    fun validate(item: LockerSummary): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerSummaryValidator(): LockerSummaryValidator {
    return LockerSummaryValidator(
        mutableListOf<LockerSummaryRule>(
            LockerSummaryPriorityRangeRule(255, 1000),
            LockerSummaryRequiredTimezoneRule("locker summary needs an entry"),
            LockerSummaryRequiredNameRule("locker summary needs an entry"),
            LockerSummaryAllowedTimezoneRule(mutableListOf<String>("manual", "rolling")),
            LockerSummaryRouteCodeLengthRule(120),
            LockerSummaryActiveGuardRule(0.4),
        ),
    )
}
