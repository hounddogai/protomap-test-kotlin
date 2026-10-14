package com.acme.transport.lastmile.locker.lockerprofile

/** A rule a fallback locker plan must pass. Check returns its problems, or none. */
interface FallbackLockerPlanRule {
    fun check(item: FallbackLockerPlan): MutableList<String>
}

data class FallbackLockerPlanCodeLengthRule(var maxLength: Int) : FallbackLockerPlanRule {
    override fun check(item: FallbackLockerPlan): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLockerPlanMaximumRangeRule(var min: Double, var max: Double) : FallbackLockerPlanRule {
    override fun check(item: FallbackLockerPlan): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class FallbackLockerPlanTitleLengthRule(var maxLength: Int) : FallbackLockerPlanRule {
    override fun check(item: FallbackLockerPlan): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLockerPlanFuelLitersRangeRule(var min: Double, var max: Double) : FallbackLockerPlanRule {
    override fun check(item: FallbackLockerPlan): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLockerPlanCodePrefixRule(var prefix: String) : FallbackLockerPlanRule {
    override fun check(item: FallbackLockerPlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLockerPlanRequiredCodeRule(var message: String) : FallbackLockerPlanRule {
    override fun check(item: FallbackLockerPlan): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback locker plan. */
data class FallbackLockerPlanValidator(var rules: MutableList<FallbackLockerPlanRule>) {
    fun validate(item: FallbackLockerPlan): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackLockerPlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackLockerPlan>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackLockerPlanValidator(): FallbackLockerPlanValidator {
    return FallbackLockerPlanValidator(
        mutableListOf<FallbackLockerPlanRule>(
            FallbackLockerPlanCodeLengthRule(80),
            FallbackLockerPlanMaximumRangeRule(7.8, 3693.7),
            FallbackLockerPlanTitleLengthRule(32),
            FallbackLockerPlanFuelLitersRangeRule(7.5, 743.7),
            FallbackLockerPlanCodePrefixRule("REF"),
            FallbackLockerPlanRequiredCodeRule("fallback locker plan needs this field"),
        ),
    )
}
