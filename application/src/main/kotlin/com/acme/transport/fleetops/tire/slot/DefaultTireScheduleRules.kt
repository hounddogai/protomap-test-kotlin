package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.text.normalize

/** A rule a default tire schedule must pass. Check returns its problems, or none. */
interface DefaultTireScheduleRule {
    fun check(item: DefaultTireSchedule): MutableList<String>
}

data class DefaultTireScheduleAllowedNameRule(var allowed: MutableList<String>) : DefaultTireScheduleRule {
    override fun check(item: DefaultTireSchedule): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTireScheduleRequiredContactEmailRule(var message: String) : DefaultTireScheduleRule {
    override fun check(item: DefaultTireSchedule): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTireScheduleNameLengthRule(var maxLength: Int) : DefaultTireScheduleRule {
    override fun check(item: DefaultTireSchedule): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTireScheduleBatchSizeRangeRule(var min: Int, var max: Int) : DefaultTireScheduleRule {
    override fun check(item: DefaultTireSchedule): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class DefaultTireScheduleReviewerLengthRule(var maxLength: Int) : DefaultTireScheduleRule {
    override fun check(item: DefaultTireSchedule): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTireScheduleCodePrefixRule(var prefix: String) : DefaultTireScheduleRule {
    override fun check(item: DefaultTireSchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default tire schedule. */
data class DefaultTireScheduleValidator(var rules: MutableList<DefaultTireScheduleRule>) {
    fun validate(item: DefaultTireSchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultTireSchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTireSchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTireScheduleValidator(): DefaultTireScheduleValidator {
    return DefaultTireScheduleValidator(
        mutableListOf<DefaultTireScheduleRule>(
            DefaultTireScheduleAllowedNameRule(mutableListOf<String>()),
            DefaultTireScheduleRequiredContactEmailRule("default tire schedule needs a value"),
            DefaultTireScheduleNameLengthRule(3600),
            DefaultTireScheduleBatchSizeRangeRule(16, 86400),
            DefaultTireScheduleReviewerLengthRule(86400),
            DefaultTireScheduleCodePrefixRule("REF"),
        ),
    )
}
