package com.acme.transport.crewing.restbreak.variance

/** A rule a rest break variance must pass. Check returns its problems, or none. */
interface RestBreakVarianceRule {
    fun check(item: RestBreakVariance): MutableList<String>
}

data class RestBreakVarianceRequiredNameRule(var message: String) : RestBreakVarianceRule {
    override fun check(item: RestBreakVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakVarianceNameLengthRule(var maxLength: Int) : RestBreakVarianceRule {
    override fun check(item: RestBreakVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakVarianceRequiredCodeRule(var message: String) : RestBreakVarianceRule {
    override fun check(item: RestBreakVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break variance. */
data class RestBreakVarianceValidator(var rules: MutableList<RestBreakVarianceRule>) {
    fun validate(item: RestBreakVariance): MutableList<String> {
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

    fun requireValid(item: RestBreakVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakVarianceValidator(): RestBreakVarianceValidator {
    return RestBreakVarianceValidator(
        mutableListOf<RestBreakVarianceRule>(
            RestBreakVarianceRequiredNameRule("rest break variance needs this field"),
            RestBreakVarianceNameLengthRule(64),
            RestBreakVarianceRequiredCodeRule("rest break variance needs an entry"),
        ),
    )
}
