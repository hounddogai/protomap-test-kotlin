package com.acme.transport.crewing.restbreak.adjustment

/** A rule a rest break adjustment must pass. Check returns its problems, or none. */
interface RestBreakAdjustmentRule {
    fun check(item: RestBreakAdjustment): MutableList<String>
}

data class RestBreakAdjustmentCodePrefixRule(var prefix: String) : RestBreakAdjustmentRule {
    override fun check(item: RestBreakAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakAdjustmentPalletsRangeRule(var min: Int, var max: Int) : RestBreakAdjustmentRule {
    override fun check(item: RestBreakAdjustment): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class RestBreakAdjustmentRatioRangeRule(var min: Double, var max: Double) : RestBreakAdjustmentRule {
    override fun check(item: RestBreakAdjustment): MutableList<String> {
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

data class RestBreakAdjustmentReviewerLengthRule(var maxLength: Int) : RestBreakAdjustmentRule {
    override fun check(item: RestBreakAdjustment): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakAdjustmentRequiredLabelRule(var message: String) : RestBreakAdjustmentRule {
    override fun check(item: RestBreakAdjustment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakAdjustmentRequiredCodeRule(var message: String) : RestBreakAdjustmentRule {
    override fun check(item: RestBreakAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break adjustment. */
data class RestBreakAdjustmentValidator(var rules: MutableList<RestBreakAdjustmentRule>) {
    fun validate(item: RestBreakAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakAdjustmentValidator(): RestBreakAdjustmentValidator {
    return RestBreakAdjustmentValidator(
        mutableListOf<RestBreakAdjustmentRule>(
            RestBreakAdjustmentCodePrefixRule("ACM"),
            RestBreakAdjustmentPalletsRangeRule(1000, 86400),
            RestBreakAdjustmentRatioRangeRule(9.4, 4232.8),
            RestBreakAdjustmentReviewerLengthRule(64),
            RestBreakAdjustmentRequiredLabelRule("rest break adjustment needs an entry"),
            RestBreakAdjustmentRequiredCodeRule("rest break adjustment needs an entry"),
        ),
    )
}
