package com.acme.transport.linehaul.backhaul.reservation

/** A rule a backhaul group must pass. Check returns its problems, or none. */
interface BackhaulGroupRule {
    fun check(item: BackhaulGroup): MutableList<String>
}

data class BackhaulGroupRequiredCodeRule(var message: String) : BackhaulGroupRule {
    override fun check(item: BackhaulGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulGroupCodePrefixRule(var prefix: String) : BackhaulGroupRule {
    override fun check(item: BackhaulGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulGroupScoreRangeRule(var min: Double, var max: Double) : BackhaulGroupRule {
    override fun check(item: BackhaulGroup): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul group. */
data class BackhaulGroupValidator(var rules: MutableList<BackhaulGroupRule>) {
    fun validate(item: BackhaulGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulGroupValidator(): BackhaulGroupValidator {
    return BackhaulGroupValidator(
        mutableListOf<BackhaulGroupRule>(
            BackhaulGroupRequiredCodeRule("backhaul group needs a value"),
            BackhaulGroupCodePrefixRule("ACM"),
            BackhaulGroupScoreRangeRule(1.2, 628.3),
        ),
    )
}
