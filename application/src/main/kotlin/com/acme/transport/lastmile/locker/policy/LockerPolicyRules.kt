package com.acme.transport.lastmile.locker.policy

/** A rule a locker policy must pass. Check returns its problems, or none. */
interface LockerPolicyRule {
    fun check(item: LockerPolicy): MutableList<String>
}

data class LockerPolicyStartsAtRangeRule(var min: Int, var max: Int) : LockerPolicyRule {
    override fun check(item: LockerPolicy): MutableList<String> {
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

data class LockerPolicyDiscountRangeRule(var min: Double, var max: Double) : LockerPolicyRule {
    override fun check(item: LockerPolicy): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerPolicyTitleLengthRule(var maxLength: Int) : LockerPolicyRule {
    override fun check(item: LockerPolicy): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerPolicyQuantityRangeRule(var min: Int, var max: Int) : LockerPolicyRule {
    override fun check(item: LockerPolicy): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerPolicyRequiredCodeRule(var message: String) : LockerPolicyRule {
    override fun check(item: LockerPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerPolicyCodeLengthRule(var maxLength: Int) : LockerPolicyRule {
    override fun check(item: LockerPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker policy. */
data class LockerPolicyValidator(var rules: MutableList<LockerPolicyRule>) {
    fun validate(item: LockerPolicy): MutableList<String> {
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

    fun requireValid(item: LockerPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerPolicyValidator(): LockerPolicyValidator {
    return LockerPolicyValidator(
        mutableListOf<LockerPolicyRule>(
            LockerPolicyStartsAtRangeRule(80, 255),
            LockerPolicyDiscountRangeRule(2.1, 2728.7),
            LockerPolicyTitleLengthRule(120),
            LockerPolicyQuantityRangeRule(1000, 3600),
            LockerPolicyRequiredCodeRule("locker policy needs a value"),
            LockerPolicyCodeLengthRule(80),
        ),
    )
}
