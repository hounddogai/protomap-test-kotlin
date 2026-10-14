package com.acme.transport.freightpay.accessorial.accessorialplan

/** A rule a seasonal accessorial tier must pass. Check returns its problems, or none. */
interface SeasonalAccessorialTierRule {
    fun check(item: SeasonalAccessorialTier): MutableList<String>
}

data class SeasonalAccessorialTierLabelLengthRule(var maxLength: Int) : SeasonalAccessorialTierRule {
    override fun check(item: SeasonalAccessorialTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialTierCodePrefixRule(var prefix: String) : SeasonalAccessorialTierRule {
    override fun check(item: SeasonalAccessorialTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialTierCodeLengthRule(var maxLength: Int) : SeasonalAccessorialTierRule {
    override fun check(item: SeasonalAccessorialTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialTierRequiredRouteCodeRule(var message: String) : SeasonalAccessorialTierRule {
    override fun check(item: SeasonalAccessorialTier): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAccessorialTierAttemptsRangeRule(var min: Int, var max: Int) : SeasonalAccessorialTierRule {
    override fun check(item: SeasonalAccessorialTier): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a seasonal accessorial tier. */
data class SeasonalAccessorialTierValidator(var rules: MutableList<SeasonalAccessorialTierRule>) {
    fun validate(item: SeasonalAccessorialTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalAccessorialTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAccessorialTier>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAccessorialTierValidator(): SeasonalAccessorialTierValidator {
    return SeasonalAccessorialTierValidator(
        mutableListOf<SeasonalAccessorialTierRule>(
            SeasonalAccessorialTierLabelLengthRule(3600),
            SeasonalAccessorialTierCodePrefixRule("Q"),
            SeasonalAccessorialTierCodeLengthRule(80),
            SeasonalAccessorialTierRequiredRouteCodeRule("seasonal accessorial tier needs a value"),
            SeasonalAccessorialTierAttemptsRangeRule(16, 80),
        ),
    )
}
