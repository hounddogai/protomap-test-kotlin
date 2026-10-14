package com.acme.transport.safety.coaching.target

/** A rule a coaching target must pass. Check returns its problems, or none. */
interface CoachingTargetRule {
    fun check(item: CoachingTarget): MutableList<String>
}

data class CoachingTargetWeightRangeRule(var min: Double, var max: Double) : CoachingTargetRule {
    override fun check(item: CoachingTarget): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class CoachingTargetCodePrefixRule(var prefix: String) : CoachingTargetRule {
    override fun check(item: CoachingTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CoachingTargetRequiredCodeRule(var message: String) : CoachingTargetRule {
    override fun check(item: CoachingTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingTargetFloorValueRangeRule(var min: Double, var max: Double) : CoachingTargetRule {
    override fun check(item: CoachingTarget): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class CoachingTargetCodeLengthRule(var maxLength: Int) : CoachingTargetRule {
    override fun check(item: CoachingTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingTargetDistanceKmRangeRule(var min: Double, var max: Double) : CoachingTargetRule {
    override fun check(item: CoachingTarget): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching target. */
data class CoachingTargetValidator(var rules: MutableList<CoachingTargetRule>) {
    fun validate(item: CoachingTarget): MutableList<String> {
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

    fun requireValid(item: CoachingTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingTargetValidator(): CoachingTargetValidator {
    return CoachingTargetValidator(
        mutableListOf<CoachingTargetRule>(
            CoachingTargetWeightRangeRule(7.9, 2324.3),
            CoachingTargetCodePrefixRule("REF"),
            CoachingTargetRequiredCodeRule("coaching target needs an entry"),
            CoachingTargetFloorValueRangeRule(7.7, 3618.4),
            CoachingTargetCodeLengthRule(80),
            CoachingTargetDistanceKmRangeRule(1.1, 2303.2),
        ),
    )
}
