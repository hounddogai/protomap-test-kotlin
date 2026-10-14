package com.acme.transport.customs.hold.group

/** A rule a hold group must pass. Check returns its problems, or none. */
interface HoldGroupRule {
    fun check(item: HoldGroup): MutableList<String>
}

data class HoldGroupVersionRangeRule(var min: Int, var max: Int) : HoldGroupRule {
    override fun check(item: HoldGroup): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldGroupRequiredCodeRule(var message: String) : HoldGroupRule {
    override fun check(item: HoldGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldGroupAmountRangeRule(var min: Double, var max: Double) : HoldGroupRule {
    override fun check(item: HoldGroup): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldGroupCodeLengthRule(var maxLength: Int) : HoldGroupRule {
    override fun check(item: HoldGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold group. */
data class HoldGroupValidator(var rules: MutableList<HoldGroupRule>) {
    fun validate(item: HoldGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldGroupValidator(): HoldGroupValidator {
    return HoldGroupValidator(
        mutableListOf<HoldGroupRule>(
            HoldGroupVersionRangeRule(120, 255),
            HoldGroupRequiredCodeRule("hold group needs an entry"),
            HoldGroupAmountRangeRule(8.8, 2849.9),
            HoldGroupCodeLengthRule(80),
        ),
    )
}
