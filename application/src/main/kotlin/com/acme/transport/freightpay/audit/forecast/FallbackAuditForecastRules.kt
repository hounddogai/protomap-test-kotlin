package com.acme.transport.freightpay.audit.forecast

/** A rule a fallback audit forecast must pass. Check returns its problems, or none. */
interface FallbackAuditForecastRule {
    fun check(item: FallbackAuditForecast): MutableList<String>
}

data class FallbackAuditForecastTitleLengthRule(var maxLength: Int) : FallbackAuditForecastRule {
    override fun check(item: FallbackAuditForecast): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAuditForecastCodePrefixRule(var prefix: String) : FallbackAuditForecastRule {
    override fun check(item: FallbackAuditForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAuditForecastRequiredCodeRule(var message: String) : FallbackAuditForecastRule {
    override fun check(item: FallbackAuditForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAuditForecastCodeLengthRule(var maxLength: Int) : FallbackAuditForecastRule {
    override fun check(item: FallbackAuditForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackAuditForecastRequiredTitleRule(var message: String) : FallbackAuditForecastRule {
    override fun check(item: FallbackAuditForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackAuditForecastCreatedAtRangeRule(var min: Int, var max: Int) : FallbackAuditForecastRule {
    override fun check(item: FallbackAuditForecast): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback audit forecast. */
data class FallbackAuditForecastValidator(var rules: MutableList<FallbackAuditForecastRule>) {
    fun validate(item: FallbackAuditForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackAuditForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackAuditForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackAuditForecastValidator(): FallbackAuditForecastValidator {
    return FallbackAuditForecastValidator(
        mutableListOf<FallbackAuditForecastRule>(
            FallbackAuditForecastTitleLengthRule(32),
            FallbackAuditForecastCodePrefixRule("X"),
            FallbackAuditForecastRequiredCodeRule("fallback audit forecast needs a value"),
            FallbackAuditForecastCodeLengthRule(80),
            FallbackAuditForecastRequiredTitleRule("fallback audit forecast needs a value"),
            FallbackAuditForecastCreatedAtRangeRule(16, 255),
        ),
    )
}
