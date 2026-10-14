package com.acme.transport.fleetops.tire.budget

/** A rule a rolling tire budget must pass. Check returns its problems, or none. */
interface RollingTireBudgetRule {
    fun check(item: RollingTireBudget): MutableList<String>
}

data class RollingTireBudgetVersionRangeRule(var min: Int, var max: Int) : RollingTireBudgetRule {
    override fun check(item: RollingTireBudget): MutableList<String> {
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

data class RollingTireBudgetCodePrefixRule(var prefix: String) : RollingTireBudgetRule {
    override fun check(item: RollingTireBudget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingTireBudgetRequiredCodeRule(var message: String) : RollingTireBudgetRule {
    override fun check(item: RollingTireBudget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTireBudgetUpdatedAtRangeRule(var min: Int, var max: Int) : RollingTireBudgetRule {
    override fun check(item: RollingTireBudget): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingTireBudgetVehicleIdLengthRule(var maxLength: Int) : RollingTireBudgetRule {
    override fun check(item: RollingTireBudget): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling tire budget. */
data class RollingTireBudgetValidator(var rules: MutableList<RollingTireBudgetRule>) {
    fun validate(item: RollingTireBudget): MutableList<String> {
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

    fun requireValid(item: RollingTireBudget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTireBudget>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTireBudgetValidator(): RollingTireBudgetValidator {
    return RollingTireBudgetValidator(
        mutableListOf<RollingTireBudgetRule>(
            RollingTireBudgetVersionRangeRule(120, 255),
            RollingTireBudgetCodePrefixRule("Q"),
            RollingTireBudgetRequiredCodeRule("rolling tire budget needs this field"),
            RollingTireBudgetUpdatedAtRangeRule(64, 3600),
            RollingTireBudgetVehicleIdLengthRule(86400),
        ),
    )
}
