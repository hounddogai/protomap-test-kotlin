package com.acme.transport.linehaul.capacity.reservation

/** A rule a default capacity allocation must pass. Check returns its problems, or none. */
interface DefaultCapacityAllocationRule {
    fun check(item: DefaultCapacityAllocation): MutableList<String>
}

data class DefaultCapacityAllocationCodePrefixRule(var prefix: String) : DefaultCapacityAllocationRule {
    override fun check(item: DefaultCapacityAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCapacityAllocationFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : DefaultCapacityAllocationRule {
    override fun check(item: DefaultCapacityAllocation): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class DefaultCapacityAllocationScoreRangeRule(
    var min: Double,
    var max: Double,
) : DefaultCapacityAllocationRule {
    override fun check(item: DefaultCapacityAllocation): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class DefaultCapacityAllocationRequiredNameRule(var message: String) : DefaultCapacityAllocationRule {
    override fun check(item: DefaultCapacityAllocation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCapacityAllocationReviewerLengthRule(var maxLength: Int) : DefaultCapacityAllocationRule {
    override fun check(item: DefaultCapacityAllocation): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default capacity allocation. */
data class DefaultCapacityAllocationValidator(var rules: MutableList<DefaultCapacityAllocationRule>) {
    fun validate(item: DefaultCapacityAllocation): MutableList<String> {
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

    fun requireValid(item: DefaultCapacityAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCapacityAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCapacityAllocationValidator(): DefaultCapacityAllocationValidator {
    return DefaultCapacityAllocationValidator(
        mutableListOf<DefaultCapacityAllocationRule>(
            DefaultCapacityAllocationCodePrefixRule("X"),
            DefaultCapacityAllocationFloorValueRangeRule(4.4, 1107.3),
            DefaultCapacityAllocationScoreRangeRule(8.7, 4913.4),
            DefaultCapacityAllocationRequiredNameRule("default capacity allocation needs this field"),
            DefaultCapacityAllocationReviewerLengthRule(255),
        ),
    )
}
