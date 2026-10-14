package com.acme.transport.linehaul.capacity.preference

/** A rule a tiered capacity schedule must pass. Check returns its problems, or none. */
interface TieredCapacityScheduleRule {
    fun check(item: TieredCapacitySchedule): MutableList<String>
}

data class TieredCapacityScheduleCodeLengthRule(var maxLength: Int) : TieredCapacityScheduleRule {
    override fun check(item: TieredCapacitySchedule): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCapacityScheduleRequiredCodeRule(var message: String) : TieredCapacityScheduleRule {
    override fun check(item: TieredCapacitySchedule): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredCapacityScheduleCodePrefixRule(var prefix: String) : TieredCapacityScheduleRule {
    override fun check(item: TieredCapacitySchedule): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredCapacityScheduleCapacityRangeRule(var min: Int, var max: Int) : TieredCapacityScheduleRule {
    override fun check(item: TieredCapacitySchedule): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class TieredCapacityScheduleTrailerIdLengthRule(var maxLength: Int) : TieredCapacityScheduleRule {
    override fun check(item: TieredCapacitySchedule): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCapacityScheduleRequiredNameRule(var message: String) : TieredCapacityScheduleRule {
    override fun check(item: TieredCapacitySchedule): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered capacity schedule. */
data class TieredCapacityScheduleValidator(var rules: MutableList<TieredCapacityScheduleRule>) {
    fun validate(item: TieredCapacitySchedule): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredCapacitySchedule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCapacitySchedule>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCapacityScheduleValidator(): TieredCapacityScheduleValidator {
    return TieredCapacityScheduleValidator(
        mutableListOf<TieredCapacityScheduleRule>(
            TieredCapacityScheduleCodeLengthRule(1000),
            TieredCapacityScheduleRequiredCodeRule("tiered capacity schedule needs this field"),
            TieredCapacityScheduleCodePrefixRule("Q"),
            TieredCapacityScheduleCapacityRangeRule(120, 86400),
            TieredCapacityScheduleTrailerIdLengthRule(1000),
            TieredCapacityScheduleRequiredNameRule("tiered capacity schedule needs this field"),
        ),
    )
}
