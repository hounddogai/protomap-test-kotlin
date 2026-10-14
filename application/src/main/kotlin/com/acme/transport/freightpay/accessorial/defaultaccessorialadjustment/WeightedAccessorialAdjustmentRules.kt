package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

/** A rule a weighted accessorial adjustment must pass. Check returns its problems, or none. */
interface WeightedAccessorialAdjustmentRule {
    fun check(item: WeightedAccessorialAdjustment): MutableList<String>
}

data class WeightedAccessorialAdjustmentNameLengthRule(var maxLength: Int) : WeightedAccessorialAdjustmentRule {
    override fun check(item: WeightedAccessorialAdjustment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAccessorialAdjustmentRequiredNameRule(
    var message: String,
) : WeightedAccessorialAdjustmentRule {
    override fun check(item: WeightedAccessorialAdjustment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAccessorialAdjustmentRequiredCodeRule(
    var message: String,
) : WeightedAccessorialAdjustmentRule {
    override fun check(item: WeightedAccessorialAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAccessorialAdjustmentRouteCodeLengthRule(
    var maxLength: Int,
) : WeightedAccessorialAdjustmentRule {
    override fun check(item: WeightedAccessorialAdjustment): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAccessorialAdjustmentCurrencyLengthRule(
    var maxLength: Int,
) : WeightedAccessorialAdjustmentRule {
    override fun check(item: WeightedAccessorialAdjustment): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAccessorialAdjustmentRequiredRouteCodeRule(
    var message: String,
) : WeightedAccessorialAdjustmentRule {
    override fun check(item: WeightedAccessorialAdjustment): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted accessorial adjustment. */
data class WeightedAccessorialAdjustmentValidator(var rules: MutableList<WeightedAccessorialAdjustmentRule>) {
    fun validate(item: WeightedAccessorialAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedAccessorialAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAccessorialAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAccessorialAdjustmentValidator(): WeightedAccessorialAdjustmentValidator {
    return WeightedAccessorialAdjustmentValidator(
        mutableListOf<WeightedAccessorialAdjustmentRule>(
            WeightedAccessorialAdjustmentNameLengthRule(64),
            WeightedAccessorialAdjustmentRequiredNameRule("weighted accessorial adjustment needs a value"),
            WeightedAccessorialAdjustmentRequiredCodeRule("weighted accessorial adjustment needs an entry"),
            WeightedAccessorialAdjustmentRouteCodeLengthRule(64),
            WeightedAccessorialAdjustmentCurrencyLengthRule(16),
            WeightedAccessorialAdjustmentRequiredRouteCodeRule("weighted accessorial adjustment needs an entry"),
        ),
    )
}
