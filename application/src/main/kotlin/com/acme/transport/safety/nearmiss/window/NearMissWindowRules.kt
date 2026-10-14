package com.acme.transport.safety.nearmiss.window

/** A rule a near miss window must pass. Check returns its problems, or none. */
interface NearMissWindowRule {
    fun check(item: NearMissWindow): MutableList<String>
}

data class NearMissWindowAmountRangeRule(var min: Double, var max: Double) : NearMissWindowRule {
    override fun check(item: NearMissWindow): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NearMissWindowApprovedGuardRule(var limit: Double) : NearMissWindowRule {
    override fun check(item: NearMissWindow): MutableList<String> {
        if (item.approved && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class NearMissWindowCodeLengthRule(var maxLength: Int) : NearMissWindowRule {
    override fun check(item: NearMissWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissWindowRateRangeRule(var min: Double, var max: Double) : NearMissWindowRule {
    override fun check(item: NearMissWindow): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a near miss window. */
data class NearMissWindowValidator(var rules: MutableList<NearMissWindowRule>) {
    fun validate(item: NearMissWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NearMissWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissWindowValidator(): NearMissWindowValidator {
    return NearMissWindowValidator(
        mutableListOf<NearMissWindowRule>(
            NearMissWindowAmountRangeRule(7.6, 2279.7),
            NearMissWindowApprovedGuardRule(2.2),
            NearMissWindowCodeLengthRule(255),
            NearMissWindowRateRangeRule(2.2, 639.4),
        ),
    )
}
