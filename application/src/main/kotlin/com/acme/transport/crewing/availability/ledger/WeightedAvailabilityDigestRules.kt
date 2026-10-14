package com.acme.transport.crewing.availability.ledger

/** A rule a weighted availability digest must pass. Check returns its problems, or none. */
interface WeightedAvailabilityDigestRule {
    fun check(item: WeightedAvailabilityDigest): MutableList<String>
}

data class WeightedAvailabilityDigestCodeLengthRule(var maxLength: Int) : WeightedAvailabilityDigestRule {
    override fun check(item: WeightedAvailabilityDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedAvailabilityDigestRequiredCodeRule(var message: String) : WeightedAvailabilityDigestRule {
    override fun check(item: WeightedAvailabilityDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedAvailabilityDigestStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : WeightedAvailabilityDigestRule {
    override fun check(item: WeightedAvailabilityDigest): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class WeightedAvailabilityDigestRequiredDepotRule(var message: String) : WeightedAvailabilityDigestRule {
    override fun check(item: WeightedAvailabilityDigest): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted availability digest. */
data class WeightedAvailabilityDigestValidator(var rules: MutableList<WeightedAvailabilityDigestRule>) {
    fun validate(item: WeightedAvailabilityDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: WeightedAvailabilityDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedAvailabilityDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedAvailabilityDigestValidator(): WeightedAvailabilityDigestValidator {
    return WeightedAvailabilityDigestValidator(
        mutableListOf<WeightedAvailabilityDigestRule>(
            WeightedAvailabilityDigestCodeLengthRule(120),
            WeightedAvailabilityDigestRequiredCodeRule("weighted availability digest needs this field"),
            WeightedAvailabilityDigestStartsAtRangeRule(32, 120),
            WeightedAvailabilityDigestRequiredDepotRule("weighted availability digest needs this field"),
        ),
    )
}
