package com.acme.transport.linehaul.capacity.capacityreservation

import com.acme.transport.kit.text.normalize

/** A rule a capacity estimate must pass. Check returns its problems, or none. */
interface CapacityEstimateRule {
    fun check(item: CapacityEstimate): MutableList<String>
}

data class CapacityEstimateAllowedTitleRule(var allowed: MutableList<String>) : CapacityEstimateRule {
    override fun check(item: CapacityEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

data class CapacityEstimateRequiredOwnerIdRule(var message: String) : CapacityEstimateRule {
    override fun check(item: CapacityEstimate): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityEstimateRatioRangeRule(var min: Double, var max: Double) : CapacityEstimateRule {
    override fun check(item: CapacityEstimate): MutableList<String> {
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

data class CapacityEstimateRequiredCodeRule(var message: String) : CapacityEstimateRule {
    override fun check(item: CapacityEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityEstimateCodeLengthRule(var maxLength: Int) : CapacityEstimateRule {
    override fun check(item: CapacityEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity estimate. */
data class CapacityEstimateValidator(var rules: MutableList<CapacityEstimateRule>) {
    fun validate(item: CapacityEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityEstimateValidator(): CapacityEstimateValidator {
    return CapacityEstimateValidator(
        mutableListOf<CapacityEstimateRule>(
            CapacityEstimateAllowedTitleRule(mutableListOf<String>()),
            CapacityEstimateRequiredOwnerIdRule("capacity estimate needs an entry"),
            CapacityEstimateRatioRangeRule(1.8, 336.9),
            CapacityEstimateRequiredCodeRule("capacity estimate needs this field"),
            CapacityEstimateCodeLengthRule(1000),
        ),
    )
}
