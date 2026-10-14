package com.acme.transport.lastmile.parcel.plan

/** A rule a lenient parcel policy must pass. Check returns its problems, or none. */
interface LenientParcelPolicyRule {
    fun check(item: LenientParcelPolicy): MutableList<String>
}

data class LenientParcelPolicyArchivedGuardRule(var limit: Double) : LenientParcelPolicyRule {
    override fun check(item: LenientParcelPolicy): MutableList<String> {
        if (item.archived && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelPolicyRequiredCategoryRule(var message: String) : LenientParcelPolicyRule {
    override fun check(item: LenientParcelPolicy): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelPolicyCodePrefixRule(var prefix: String) : LenientParcelPolicyRule {
    override fun check(item: LenientParcelPolicy): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientParcelPolicyMaximumRangeRule(var min: Double, var max: Double) : LenientParcelPolicyRule {
    override fun check(item: LenientParcelPolicy): MutableList<String> {
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

data class LenientParcelPolicyCategoryLengthRule(var maxLength: Int) : LenientParcelPolicyRule {
    override fun check(item: LenientParcelPolicy): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient parcel policy. */
data class LenientParcelPolicyValidator(var rules: MutableList<LenientParcelPolicyRule>) {
    fun validate(item: LenientParcelPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientParcelPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientParcelPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientParcelPolicyValidator(): LenientParcelPolicyValidator {
    return LenientParcelPolicyValidator(
        mutableListOf<LenientParcelPolicyRule>(
            LenientParcelPolicyArchivedGuardRule(5.1),
            LenientParcelPolicyRequiredCategoryRule("lenient parcel policy needs an entry"),
            LenientParcelPolicyCodePrefixRule("X"),
            LenientParcelPolicyMaximumRangeRule(2.8, 154.9),
            LenientParcelPolicyCategoryLengthRule(32),
        ),
    )
}
