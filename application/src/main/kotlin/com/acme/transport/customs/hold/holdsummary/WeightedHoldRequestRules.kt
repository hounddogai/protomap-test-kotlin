package com.acme.transport.customs.hold.holdsummary

/** A rule a weighted hold request must pass. Check returns its problems, or none. */
interface WeightedHoldRequestRule {
    fun check(item: WeightedHoldRequest): MutableList<String>
}

data class WeightedHoldRequestSummaryLengthRule(var maxLength: Int) : WeightedHoldRequestRule {
    override fun check(item: WeightedHoldRequest): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedHoldRequestRequiredCodeRule(var message: String) : WeightedHoldRequestRule {
    override fun check(item: WeightedHoldRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedHoldRequestCodePrefixRule(var prefix: String) : WeightedHoldRequestRule {
    override fun check(item: WeightedHoldRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedHoldRequestRequiredNameRule(var message: String) : WeightedHoldRequestRule {
    override fun check(item: WeightedHoldRequest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedHoldRequestCapacityRangeRule(var min: Int, var max: Int) : WeightedHoldRequestRule {
    override fun check(item: WeightedHoldRequest): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted hold request. */
data class WeightedHoldRequestValidator(var rules: MutableList<WeightedHoldRequestRule>) {
    fun validate(item: WeightedHoldRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedHoldRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedHoldRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedHoldRequestValidator(): WeightedHoldRequestValidator {
    return WeightedHoldRequestValidator(
        mutableListOf<WeightedHoldRequestRule>(
            WeightedHoldRequestSummaryLengthRule(64),
            WeightedHoldRequestRequiredCodeRule("weighted hold request needs a value"),
            WeightedHoldRequestCodePrefixRule("OPS"),
            WeightedHoldRequestRequiredNameRule("weighted hold request needs a value"),
            WeightedHoldRequestCapacityRangeRule(32, 120),
        ),
    )
}
