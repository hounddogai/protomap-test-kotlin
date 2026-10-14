package com.acme.transport.crewing.restbreak.summary

/** A rule a rest break template must pass. Check returns its problems, or none. */
interface RestBreakTemplateRule {
    fun check(item: RestBreakTemplate): MutableList<String>
}

data class RestBreakTemplateWeightRangeRule(var min: Double, var max: Double) : RestBreakTemplateRule {
    override fun check(item: RestBreakTemplate): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakTemplateScoreRangeRule(var min: Double, var max: Double) : RestBreakTemplateRule {
    override fun check(item: RestBreakTemplate): MutableList<String> {
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

data class RestBreakTemplateNameLengthRule(var maxLength: Int) : RestBreakTemplateRule {
    override fun check(item: RestBreakTemplate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakTemplateCodePrefixRule(var prefix: String) : RestBreakTemplateRule {
    override fun check(item: RestBreakTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakTemplateRequiredDockRule(var message: String) : RestBreakTemplateRule {
    override fun check(item: RestBreakTemplate): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break template. */
data class RestBreakTemplateValidator(var rules: MutableList<RestBreakTemplateRule>) {
    fun validate(item: RestBreakTemplate): MutableList<String> {
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

    fun requireValid(item: RestBreakTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakTemplateValidator(): RestBreakTemplateValidator {
    return RestBreakTemplateValidator(
        mutableListOf<RestBreakTemplateRule>(
            RestBreakTemplateWeightRangeRule(5.1, 3225.7),
            RestBreakTemplateScoreRangeRule(5.6, 4642.1),
            RestBreakTemplateNameLengthRule(64),
            RestBreakTemplateCodePrefixRule("ACM"),
            RestBreakTemplateRequiredDockRule("rest break template needs an entry"),
        ),
    )
}
