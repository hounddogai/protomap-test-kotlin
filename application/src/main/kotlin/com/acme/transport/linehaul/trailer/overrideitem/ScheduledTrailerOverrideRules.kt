package com.acme.transport.linehaul.trailer.overrideitem

/** A rule a scheduled trailer override must pass. Check returns its problems, or none. */
interface ScheduledTrailerOverrideRule {
    fun check(item: ScheduledTrailerOverride): MutableList<String>
}

data class ScheduledTrailerOverrideNameLengthRule(var maxLength: Int) : ScheduledTrailerOverrideRule {
    override fun check(item: ScheduledTrailerOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerOverrideCodePrefixRule(var prefix: String) : ScheduledTrailerOverrideRule {
    override fun check(item: ScheduledTrailerOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerOverrideRouteCodeLengthRule(var maxLength: Int) : ScheduledTrailerOverrideRule {
    override fun check(item: ScheduledTrailerOverride): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerOverrideScoreRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledTrailerOverrideRule {
    override fun check(item: ScheduledTrailerOverride): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled trailer override. */
data class ScheduledTrailerOverrideValidator(var rules: MutableList<ScheduledTrailerOverrideRule>) {
    fun validate(item: ScheduledTrailerOverride): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ScheduledTrailerOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTrailerOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTrailerOverrideValidator(): ScheduledTrailerOverrideValidator {
    return ScheduledTrailerOverrideValidator(
        mutableListOf<ScheduledTrailerOverrideRule>(
            ScheduledTrailerOverrideNameLengthRule(64),
            ScheduledTrailerOverrideCodePrefixRule("ACM"),
            ScheduledTrailerOverrideRouteCodeLengthRule(32),
            ScheduledTrailerOverrideScoreRangeRule(9.0, 852.2),
        ),
    )
}
