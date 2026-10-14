package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.text.normalize

/** A rule a scheduled locker group must pass. Check returns its problems, or none. */
interface ScheduledLockerGroupRule {
    fun check(item: ScheduledLockerGroup): MutableList<String>
}

data class ScheduledLockerGroupDriverIdLengthRule(var maxLength: Int) : ScheduledLockerGroupRule {
    override fun check(item: ScheduledLockerGroup): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerGroupRequiredCodeRule(var message: String) : ScheduledLockerGroupRule {
    override fun check(item: ScheduledLockerGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerGroupRequiredRegionRule(var message: String) : ScheduledLockerGroupRule {
    override fun check(item: ScheduledLockerGroup): MutableList<String> {
        if (item.region.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerGroupLoadKgRangeRule(var min: Double, var max: Double) : ScheduledLockerGroupRule {
    override fun check(item: ScheduledLockerGroup): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class ScheduledLockerGroupAllowedNameRule(var allowed: MutableList<String>) : ScheduledLockerGroupRule {
    override fun check(item: ScheduledLockerGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledLockerGroupNameLengthRule(var maxLength: Int) : ScheduledLockerGroupRule {
    override fun check(item: ScheduledLockerGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled locker group. */
data class ScheduledLockerGroupValidator(var rules: MutableList<ScheduledLockerGroupRule>) {
    fun validate(item: ScheduledLockerGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledLockerGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledLockerGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledLockerGroupValidator(): ScheduledLockerGroupValidator {
    return ScheduledLockerGroupValidator(
        mutableListOf<ScheduledLockerGroupRule>(
            ScheduledLockerGroupDriverIdLengthRule(64),
            ScheduledLockerGroupRequiredCodeRule("scheduled locker group needs this field"),
            ScheduledLockerGroupRequiredRegionRule("scheduled locker group needs a value"),
            ScheduledLockerGroupLoadKgRangeRule(0.6, 3247.4),
            ScheduledLockerGroupAllowedNameRule(mutableListOf<String>("fallback", "strict", "lenient")),
            ScheduledLockerGroupNameLengthRule(3600),
        ),
    )
}
