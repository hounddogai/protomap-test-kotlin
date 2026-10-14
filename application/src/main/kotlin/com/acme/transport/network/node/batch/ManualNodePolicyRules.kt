package com.acme.transport.network.node.batch

/** A rule a manual node policy must pass. Check returns its problems, or none. */
interface ManualNodePolicyRule {
    fun check(item: ManualNodePolicy): MutableList<String>
}

data class ManualNodePolicyRequiredTitleRule(var message: String) : ManualNodePolicyRule {
    override fun check(item: ManualNodePolicy): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualNodePolicyLocaleLengthRule(var maxLength: Int) : ManualNodePolicyRule {
    override fun check(item: ManualNodePolicy): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualNodePolicyLimitRangeRule(var min: Int, var max: Int) : ManualNodePolicyRule {
    override fun check(item: ManualNodePolicy): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualNodePolicyRequiredCodeRule(var message: String) : ManualNodePolicyRule {
    override fun check(item: ManualNodePolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualNodePolicyGraceDaysRangeRule(var min: Int, var max: Int) : ManualNodePolicyRule {
    override fun check(item: ManualNodePolicy): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual node policy. */
data class ManualNodePolicyValidator(var rules: MutableList<ManualNodePolicyRule>) {
    fun validate(item: ManualNodePolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualNodePolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualNodePolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultManualNodePolicyValidator(): ManualNodePolicyValidator {
    return ManualNodePolicyValidator(
        mutableListOf<ManualNodePolicyRule>(
            ManualNodePolicyRequiredTitleRule("manual node policy needs a value"),
            ManualNodePolicyLocaleLengthRule(86400),
            ManualNodePolicyLimitRangeRule(120, 3600),
            ManualNodePolicyRequiredCodeRule("manual node policy needs a value"),
            ManualNodePolicyGraceDaysRangeRule(255, 1000),
        ),
    )
}
