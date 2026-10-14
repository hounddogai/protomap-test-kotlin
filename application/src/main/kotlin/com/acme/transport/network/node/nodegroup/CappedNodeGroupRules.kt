package com.acme.transport.network.node.nodegroup

/** A rule a capped node group must pass. Check returns its problems, or none. */
interface CappedNodeGroupRule {
    fun check(item: CappedNodeGroup): MutableList<String>
}

data class CappedNodeGroupLimitRangeRule(var min: Int, var max: Int) : CappedNodeGroupRule {
    override fun check(item: CappedNodeGroup): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class CappedNodeGroupCarrierLengthRule(var maxLength: Int) : CappedNodeGroupRule {
    override fun check(item: CappedNodeGroup): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedNodeGroupCategoryLengthRule(var maxLength: Int) : CappedNodeGroupRule {
    override fun check(item: CappedNodeGroup): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedNodeGroupCodePrefixRule(var prefix: String) : CappedNodeGroupRule {
    override fun check(item: CappedNodeGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedNodeGroupVersionRangeRule(var min: Int, var max: Int) : CappedNodeGroupRule {
    override fun check(item: CappedNodeGroup): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped node group. */
data class CappedNodeGroupValidator(var rules: MutableList<CappedNodeGroupRule>) {
    fun validate(item: CappedNodeGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedNodeGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedNodeGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedNodeGroupValidator(): CappedNodeGroupValidator {
    return CappedNodeGroupValidator(
        mutableListOf<CappedNodeGroupRule>(
            CappedNodeGroupLimitRangeRule(64, 1000),
            CappedNodeGroupCarrierLengthRule(3600),
            CappedNodeGroupCategoryLengthRule(255),
            CappedNodeGroupCodePrefixRule("ACM"),
            CappedNodeGroupVersionRangeRule(32, 32),
        ),
    )
}
