package com.acme.transport.lastmile.locker.rule

/** A rule a locker rule must pass. Check returns its problems, or none. */
interface LockerRuleRule {
    fun check(item: LockerRule): MutableList<String>
}

data class LockerRuleVersionRangeRule(var min: Int, var max: Int) : LockerRuleRule {
    override fun check(item: LockerRule): MutableList<String> {
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

data class LockerRuleVehicleIdLengthRule(var maxLength: Int) : LockerRuleRule {
    override fun check(item: LockerRule): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerRuleRequiredReviewerRule(var message: String) : LockerRuleRule {
    override fun check(item: LockerRule): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker rule. */
data class LockerRuleValidator(var rules: MutableList<LockerRuleRule>) {
    fun validate(item: LockerRule): MutableList<String> {
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

    fun requireValid(item: LockerRule) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerRule>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerRuleValidator(): LockerRuleValidator {
    return LockerRuleValidator(
        mutableListOf<LockerRuleRule>(
            LockerRuleVersionRangeRule(86400, 86400),
            LockerRuleVehicleIdLengthRule(86400),
            LockerRuleRequiredReviewerRule("locker rule needs a value"),
        ),
    )
}
