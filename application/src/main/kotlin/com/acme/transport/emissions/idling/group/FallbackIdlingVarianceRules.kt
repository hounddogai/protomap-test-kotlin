package com.acme.transport.emissions.idling.group

/** A rule a fallback idling variance must pass. Check returns its problems, or none. */
interface FallbackIdlingVarianceRule {
    fun check(item: FallbackIdlingVariance): MutableList<String>
}

data class FallbackIdlingVarianceCodeLengthRule(var maxLength: Int) : FallbackIdlingVarianceRule {
    override fun check(item: FallbackIdlingVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingVarianceAmountRangeRule(
    var min: Double,
    var max: Double,
) : FallbackIdlingVarianceRule {
    override fun check(item: FallbackIdlingVariance): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class FallbackIdlingVarianceVersionRangeRule(var min: Int, var max: Int) : FallbackIdlingVarianceRule {
    override fun check(item: FallbackIdlingVariance): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingVarianceRequiredLocaleRule(var message: String) : FallbackIdlingVarianceRule {
    override fun check(item: FallbackIdlingVariance): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingVarianceRequiredLabelRule(var message: String) : FallbackIdlingVarianceRule {
    override fun check(item: FallbackIdlingVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingVarianceLocaleLengthRule(var maxLength: Int) : FallbackIdlingVarianceRule {
    override fun check(item: FallbackIdlingVariance): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback idling variance. */
data class FallbackIdlingVarianceValidator(var rules: MutableList<FallbackIdlingVarianceRule>) {
    fun validate(item: FallbackIdlingVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackIdlingVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackIdlingVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackIdlingVarianceValidator(): FallbackIdlingVarianceValidator {
    return FallbackIdlingVarianceValidator(
        mutableListOf<FallbackIdlingVarianceRule>(
            FallbackIdlingVarianceCodeLengthRule(3600),
            FallbackIdlingVarianceAmountRangeRule(7.1, 351.5),
            FallbackIdlingVarianceVersionRangeRule(16, 32),
            FallbackIdlingVarianceRequiredLocaleRule("fallback idling variance needs a value"),
            FallbackIdlingVarianceRequiredLabelRule("fallback idling variance needs an entry"),
            FallbackIdlingVarianceLocaleLengthRule(3600),
        ),
    )
}
