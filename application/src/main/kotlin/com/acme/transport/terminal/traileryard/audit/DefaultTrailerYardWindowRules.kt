package com.acme.transport.terminal.traileryard.audit

/** A rule a default trailer yard window must pass. Check returns its problems, or none. */
interface DefaultTrailerYardWindowRule {
    fun check(item: DefaultTrailerYardWindow): MutableList<String>
}

data class DefaultTrailerYardWindowEndsAtRangeRule(var min: Int, var max: Int) : DefaultTrailerYardWindowRule {
    override fun check(item: DefaultTrailerYardWindow): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class DefaultTrailerYardWindowCodeLengthRule(var maxLength: Int) : DefaultTrailerYardWindowRule {
    override fun check(item: DefaultTrailerYardWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerYardWindowLabelLengthRule(var maxLength: Int) : DefaultTrailerYardWindowRule {
    override fun check(item: DefaultTrailerYardWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerYardWindowWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : DefaultTrailerYardWindowRule {
    override fun check(item: DefaultTrailerYardWindow): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerYardWindowDriverIdLengthRule(var maxLength: Int) : DefaultTrailerYardWindowRule {
    override fun check(item: DefaultTrailerYardWindow): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default trailer yard window. */
data class DefaultTrailerYardWindowValidator(var rules: MutableList<DefaultTrailerYardWindowRule>) {
    fun validate(item: DefaultTrailerYardWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultTrailerYardWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTrailerYardWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTrailerYardWindowValidator(): DefaultTrailerYardWindowValidator {
    return DefaultTrailerYardWindowValidator(
        mutableListOf<DefaultTrailerYardWindowRule>(
            DefaultTrailerYardWindowEndsAtRangeRule(255, 3600),
            DefaultTrailerYardWindowCodeLengthRule(80),
            DefaultTrailerYardWindowLabelLengthRule(1000),
            DefaultTrailerYardWindowWindowDaysRangeRule(32, 1000),
            DefaultTrailerYardWindowDriverIdLengthRule(86400),
        ),
    )
}
