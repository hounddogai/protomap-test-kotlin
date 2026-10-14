package com.acme.transport.linehaul.tender.exception

/** A rule a weighted tender exception must pass. Check returns its problems, or none. */
interface WeightedTenderExceptionRule {
    fun check(item: WeightedTenderException): MutableList<String>
}

data class WeightedTenderExceptionCodePrefixRule(var prefix: String) : WeightedTenderExceptionRule {
    override fun check(item: WeightedTenderException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedTenderExceptionCostRangeRule(
    var min: Double,
    var max: Double,
) : WeightedTenderExceptionRule {
    override fun check(item: WeightedTenderException): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class WeightedTenderExceptionRequiredDescriptionRule(var message: String) : WeightedTenderExceptionRule {
    override fun check(item: WeightedTenderException): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted tender exception. */
data class WeightedTenderExceptionValidator(var rules: MutableList<WeightedTenderExceptionRule>) {
    fun validate(item: WeightedTenderException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedTenderException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedTenderException>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedTenderExceptionValidator(): WeightedTenderExceptionValidator {
    return WeightedTenderExceptionValidator(
        mutableListOf<WeightedTenderExceptionRule>(
            WeightedTenderExceptionCodePrefixRule("REF"),
            WeightedTenderExceptionCostRangeRule(5.5, 486.3),
            WeightedTenderExceptionRequiredDescriptionRule("weighted tender exception needs an entry"),
        ),
    )
}
