package com.acme.transport.lastmile.parcel.metric

/** A rule a weighted parcel policy must pass. Check returns its problems, or none. */
interface WeightedParcelPolicyRule {
    fun check(item: WeightedParcelPolicy): MutableList<String>
}

data class WeightedParcelPolicyRequiredDepotRule(var message: String) : WeightedParcelPolicyRule {
    override fun check(item: WeightedParcelPolicy): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelPolicyCodeLengthRule(var maxLength: Int) : WeightedParcelPolicyRule {
    override fun check(item: WeightedParcelPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelPolicyMaximumRangeRule(var min: Double, var max: Double) : WeightedParcelPolicyRule {
    override fun check(item: WeightedParcelPolicy): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class WeightedParcelPolicyDepotLengthRule(var maxLength: Int) : WeightedParcelPolicyRule {
    override fun check(item: WeightedParcelPolicy): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted parcel policy. */
data class WeightedParcelPolicyValidator(var rules: MutableList<WeightedParcelPolicyRule>) {
    fun validate(item: WeightedParcelPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedParcelPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedParcelPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedParcelPolicyValidator(): WeightedParcelPolicyValidator {
    return WeightedParcelPolicyValidator(
        mutableListOf<WeightedParcelPolicyRule>(
            WeightedParcelPolicyRequiredDepotRule("weighted parcel policy needs an entry"),
            WeightedParcelPolicyCodeLengthRule(32),
            WeightedParcelPolicyMaximumRangeRule(8.3, 1180.9),
            WeightedParcelPolicyDepotLengthRule(1000),
        ),
    )
}
