package com.acme.transport.safety.nearmiss.budget

/** A rule a near miss budget must pass. Check returns its problems, or none. */
interface NearMissBudgetRule {
    fun check(item: NearMissBudget): MutableList<String>
}

data class NearMissBudgetCodePrefixRule(var prefix: String) : NearMissBudgetRule {
    override fun check(item: NearMissBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NearMissBudgetBudgetRangeRule(var min: Double, var max: Double) : NearMissBudgetRule {
    override fun check(item: NearMissBudget): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NearMissBudgetRequiredNameRule(var message: String) : NearMissBudgetRule {
    override fun check(item: NearMissBudget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissBudgetRequiredShiftCodeRule(var message: String) : NearMissBudgetRule {
    override fun check(item: NearMissBudget): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissBudgetContactEmailLengthRule(var maxLength: Int) : NearMissBudgetRule {
    override fun check(item: NearMissBudget): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss budget. */
data class NearMissBudgetValidator(var rules: MutableList<NearMissBudgetRule>) {
    fun validate(item: NearMissBudget): MutableList<String> {
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

    fun requireValid(item: NearMissBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissBudgetValidator(): NearMissBudgetValidator {
    return NearMissBudgetValidator(
        mutableListOf<NearMissBudgetRule>(
            NearMissBudgetCodePrefixRule("ACM"),
            NearMissBudgetBudgetRangeRule(4.1, 4819.7),
            NearMissBudgetRequiredNameRule("near miss budget needs a value"),
            NearMissBudgetRequiredShiftCodeRule("near miss budget needs a value"),
            NearMissBudgetContactEmailLengthRule(1000),
        ),
    )
}
