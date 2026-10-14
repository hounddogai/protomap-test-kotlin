package com.acme.transport.freightpay.accessorial.variance

/** A rule a weighted accessorial variance must pass. Check returns its problems, or none. */
interface WeightedAccessorialVarianceRule {
    fun check(item: WeightedAccessorialVariance): MutableList<String>
}

data class WeightedAccessorialVarianceCodeLengthRule(var maxLength: Int) : WeightedAccessorialVarianceRule {
    override fun check(item: WeightedAccessorialVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAccessorialVarianceTitleLengthRule(var maxLength: Int) : WeightedAccessorialVarianceRule {
    override fun check(item: WeightedAccessorialVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAccessorialVarianceRouteCodeLengthRule(
    var maxLength: Int,
) : WeightedAccessorialVarianceRule {
    override fun check(item: WeightedAccessorialVariance): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAccessorialVarianceRequiredTitleRule(var message: String) : WeightedAccessorialVarianceRule {
    override fun check(item: WeightedAccessorialVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAccessorialVarianceAmountRangeRule(
    var min: Double,
    var max: Double,
) : WeightedAccessorialVarianceRule {
    override fun check(item: WeightedAccessorialVariance): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAccessorialVariancePalletsRangeRule(
    var min: Int,
    var max: Int,
) : WeightedAccessorialVarianceRule {
    override fun check(item: WeightedAccessorialVariance): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted accessorial variance. */
data class WeightedAccessorialVarianceValidator(var rules: MutableList<WeightedAccessorialVarianceRule>) {
    fun validate(item: WeightedAccessorialVariance): MutableList<String> {
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

    fun requireValid(item: WeightedAccessorialVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAccessorialVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAccessorialVarianceValidator(): WeightedAccessorialVarianceValidator {
    return WeightedAccessorialVarianceValidator(
        mutableListOf<WeightedAccessorialVarianceRule>(
            WeightedAccessorialVarianceCodeLengthRule(32),
            WeightedAccessorialVarianceTitleLengthRule(86400),
            WeightedAccessorialVarianceRouteCodeLengthRule(80),
            WeightedAccessorialVarianceRequiredTitleRule("weighted accessorial variance needs this field"),
            WeightedAccessorialVarianceAmountRangeRule(6.0, 3965.6),
            WeightedAccessorialVariancePalletsRangeRule(16, 86400),
        ),
    )
}
