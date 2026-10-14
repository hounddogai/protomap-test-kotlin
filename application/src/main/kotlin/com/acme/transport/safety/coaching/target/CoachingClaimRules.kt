package com.acme.transport.safety.coaching.target

/** A rule a coaching claim must pass. Check returns its problems, or none. */
interface CoachingClaimRule {
    fun check(item: CoachingClaim): MutableList<String>
}

data class CoachingClaimRequiredExternalRefRule(var message: String) : CoachingClaimRule {
    override fun check(item: CoachingClaim): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CoachingClaimFuelLitersRangeRule(var min: Double, var max: Double) : CoachingClaimRule {
    override fun check(item: CoachingClaim): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class CoachingClaimLoadKgRangeRule(var min: Double, var max: Double) : CoachingClaimRule {
    override fun check(item: CoachingClaim): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CoachingClaimTitleLengthRule(var maxLength: Int) : CoachingClaimRule {
    override fun check(item: CoachingClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CoachingClaimCodePrefixRule(var prefix: String) : CoachingClaimRule {
    override fun check(item: CoachingClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a coaching claim. */
data class CoachingClaimValidator(var rules: MutableList<CoachingClaimRule>) {
    fun validate(item: CoachingClaim): MutableList<String> {
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

    fun requireValid(item: CoachingClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CoachingClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultCoachingClaimValidator(): CoachingClaimValidator {
    return CoachingClaimValidator(
        mutableListOf<CoachingClaimRule>(
            CoachingClaimRequiredExternalRefRule("coaching claim needs a value"),
            CoachingClaimFuelLitersRangeRule(6.6, 561.5),
            CoachingClaimLoadKgRangeRule(9.3, 3822.8),
            CoachingClaimTitleLengthRule(255),
            CoachingClaimCodePrefixRule("X"),
        ),
    )
}
