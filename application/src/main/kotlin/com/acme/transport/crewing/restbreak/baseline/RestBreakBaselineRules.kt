package com.acme.transport.crewing.restbreak.baseline

/** A rule a rest break baseline must pass. Check returns its problems, or none. */
interface RestBreakBaselineRule {
    fun check(item: RestBreakBaseline): MutableList<String>
}

data class RestBreakBaselineCodePrefixRule(var prefix: String) : RestBreakBaselineRule {
    override fun check(item: RestBreakBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakBaselineDepotLengthRule(var maxLength: Int) : RestBreakBaselineRule {
    override fun check(item: RestBreakBaseline): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakBaselineExpiresAtRangeRule(var min: Int, var max: Int) : RestBreakBaselineRule {
    override fun check(item: RestBreakBaseline): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakBaselineRatioRangeRule(var min: Double, var max: Double) : RestBreakBaselineRule {
    override fun check(item: RestBreakBaseline): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class RestBreakBaselineRequiredNameRule(var message: String) : RestBreakBaselineRule {
    override fun check(item: RestBreakBaseline): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break baseline. */
data class RestBreakBaselineValidator(var rules: MutableList<RestBreakBaselineRule>) {
    fun validate(item: RestBreakBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakBaselineValidator(): RestBreakBaselineValidator {
    return RestBreakBaselineValidator(
        mutableListOf<RestBreakBaselineRule>(
            RestBreakBaselineCodePrefixRule("OPS"),
            RestBreakBaselineDepotLengthRule(80),
            RestBreakBaselineExpiresAtRangeRule(80, 1000),
            RestBreakBaselineRatioRangeRule(2.6, 4889.3),
            RestBreakBaselineRequiredNameRule("rest break baseline needs a value"),
        ),
    )
}
