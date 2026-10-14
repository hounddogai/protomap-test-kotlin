package com.acme.transport.customs.declaration.threshold

/** A rule a declaration group must pass. Check returns its problems, or none. */
interface DeclarationGroupRule {
    fun check(item: DeclarationGroup): MutableList<String>
}

data class DeclarationGroupRequiredCodeRule(var message: String) : DeclarationGroupRule {
    override fun check(item: DeclarationGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationGroupCodePrefixRule(var prefix: String) : DeclarationGroupRule {
    override fun check(item: DeclarationGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationGroupScoreRangeRule(var min: Double, var max: Double) : DeclarationGroupRule {
    override fun check(item: DeclarationGroup): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationGroupBudgetRangeRule(var min: Double, var max: Double) : DeclarationGroupRule {
    override fun check(item: DeclarationGroup): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a declaration group. */
data class DeclarationGroupValidator(var rules: MutableList<DeclarationGroupRule>) {
    fun validate(item: DeclarationGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DeclarationGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationGroupValidator(): DeclarationGroupValidator {
    return DeclarationGroupValidator(
        mutableListOf<DeclarationGroupRule>(
            DeclarationGroupRequiredCodeRule("declaration group needs an entry"),
            DeclarationGroupCodePrefixRule("X"),
            DeclarationGroupScoreRangeRule(1.1, 2362.3),
            DeclarationGroupBudgetRangeRule(1.9, 4812.2),
        ),
    )
}
