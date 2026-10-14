package com.acme.transport.crewing.roster.proposal

/** A rule a strict roster baseline must pass. Check returns its problems, or none. */
interface StrictRosterBaselineRule {
    fun check(item: StrictRosterBaseline): MutableList<String>
}

data class StrictRosterBaselineLabelLengthRule(var maxLength: Int) : StrictRosterBaselineRule {
    override fun check(item: StrictRosterBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRosterBaselineTitleLengthRule(var maxLength: Int) : StrictRosterBaselineRule {
    override fun check(item: StrictRosterBaseline): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRosterBaselineExpiresAtRangeRule(var min: Int, var max: Int) : StrictRosterBaselineRule {
    override fun check(item: StrictRosterBaseline): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class StrictRosterBaselineCodePrefixRule(var prefix: String) : StrictRosterBaselineRule {
    override fun check(item: StrictRosterBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict roster baseline. */
data class StrictRosterBaselineValidator(var rules: MutableList<StrictRosterBaselineRule>) {
    fun validate(item: StrictRosterBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRosterBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRosterBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRosterBaselineValidator(): StrictRosterBaselineValidator {
    return StrictRosterBaselineValidator(
        mutableListOf<StrictRosterBaselineRule>(
            StrictRosterBaselineLabelLengthRule(64),
            StrictRosterBaselineTitleLengthRule(16),
            StrictRosterBaselineExpiresAtRangeRule(1000, 3600),
            StrictRosterBaselineCodePrefixRule("REF"),
        ),
    )
}
