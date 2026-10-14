package com.acme.transport.safety.violation.exception

/** A rule a violation forecast must pass. Check returns its problems, or none. */
interface ViolationForecastRule {
    fun check(item: ViolationForecast): MutableList<String>
}

data class ViolationForecastCodeLengthRule(var maxLength: Int) : ViolationForecastRule {
    override fun check(item: ViolationForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationForecastCodePrefixRule(var prefix: String) : ViolationForecastRule {
    override fun check(item: ViolationForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ViolationForecastPinnedGuardRule(var limit: Int) : ViolationForecastRule {
    override fun check(item: ViolationForecast): MutableList<String> {
        if (item.pinned && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation forecast. */
data class ViolationForecastValidator(var rules: MutableList<ViolationForecastRule>) {
    fun validate(item: ViolationForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationForecastValidator(): ViolationForecastValidator {
    return ViolationForecastValidator(
        mutableListOf<ViolationForecastRule>(
            ViolationForecastCodeLengthRule(86400),
            ViolationForecastCodePrefixRule("ACM"),
            ViolationForecastPinnedGuardRule(80),
        ),
    )
}
