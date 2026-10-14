package com.acme.transport.emissions.idling.baseline

/** A rule a idling baseline must pass. Check returns its problems, or none. */
interface IdlingBaselineRule {
    fun check(item: IdlingBaseline): MutableList<String>
}

data class IdlingBaselineRequiredLabelRule(var message: String) : IdlingBaselineRule {
    override fun check(item: IdlingBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingBaselineChannelLengthRule(var maxLength: Int) : IdlingBaselineRule {
    override fun check(item: IdlingBaseline): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IdlingBaselineRequiredCodeRule(var message: String) : IdlingBaselineRule {
    override fun check(item: IdlingBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class IdlingBaselineScoreRangeRule(var min: Double, var max: Double) : IdlingBaselineRule {
    override fun check(item: IdlingBaseline): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class IdlingBaselineCodePrefixRule(var prefix: String) : IdlingBaselineRule {
    override fun check(item: IdlingBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a idling baseline. */
data class IdlingBaselineValidator(var rules: MutableList<IdlingBaselineRule>) {
    fun validate(item: IdlingBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: IdlingBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IdlingBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultIdlingBaselineValidator(): IdlingBaselineValidator {
    return IdlingBaselineValidator(
        mutableListOf<IdlingBaselineRule>(
            IdlingBaselineRequiredLabelRule("idling baseline needs an entry"),
            IdlingBaselineChannelLengthRule(3600),
            IdlingBaselineRequiredCodeRule("idling baseline needs a value"),
            IdlingBaselineScoreRangeRule(7.0, 3916.3),
            IdlingBaselineCodePrefixRule("X"),
        ),
    )
}
