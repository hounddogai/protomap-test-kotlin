package com.acme.transport.network.node.group

/** A rule a legacy node group must pass. Check returns its problems, or none. */
interface LegacyNodeGroupRule {
    fun check(item: LegacyNodeGroup): MutableList<String>
}

data class LegacyNodeGroupRequiredCodeRule(var message: String) : LegacyNodeGroupRule {
    override fun check(item: LegacyNodeGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyNodeGroupCodeLengthRule(var maxLength: Int) : LegacyNodeGroupRule {
    override fun check(item: LegacyNodeGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyNodeGroupRequiredDriverIdRule(var message: String) : LegacyNodeGroupRule {
    override fun check(item: LegacyNodeGroup): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyNodeGroupAmountRangeRule(var min: Double, var max: Double) : LegacyNodeGroupRule {
    override fun check(item: LegacyNodeGroup): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class LegacyNodeGroupRequiredTitleRule(var message: String) : LegacyNodeGroupRule {
    override fun check(item: LegacyNodeGroup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyNodeGroupTitleLengthRule(var maxLength: Int) : LegacyNodeGroupRule {
    override fun check(item: LegacyNodeGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy node group. */
data class LegacyNodeGroupValidator(var rules: MutableList<LegacyNodeGroupRule>) {
    fun validate(item: LegacyNodeGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyNodeGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyNodeGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyNodeGroupValidator(): LegacyNodeGroupValidator {
    return LegacyNodeGroupValidator(
        mutableListOf<LegacyNodeGroupRule>(
            LegacyNodeGroupRequiredCodeRule("legacy node group needs an entry"),
            LegacyNodeGroupCodeLengthRule(3600),
            LegacyNodeGroupRequiredDriverIdRule("legacy node group needs a value"),
            LegacyNodeGroupAmountRangeRule(8.9, 3262.7),
            LegacyNodeGroupRequiredTitleRule("legacy node group needs an entry"),
            LegacyNodeGroupTitleLengthRule(255),
        ),
    )
}
