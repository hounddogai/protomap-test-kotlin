package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

/** A rule a default attempt digest must pass. Check returns its problems, or none. */
interface DefaultAttemptDigestRule {
    fun check(item: DefaultAttemptDigest): MutableList<String>
}

data class DefaultAttemptDigestFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : DefaultAttemptDigestRule {
    override fun check(item: DefaultAttemptDigest): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptDigestLimitRangeRule(var min: Int, var max: Int) : DefaultAttemptDigestRule {
    override fun check(item: DefaultAttemptDigest): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class DefaultAttemptDigestCodeLengthRule(var maxLength: Int) : DefaultAttemptDigestRule {
    override fun check(item: DefaultAttemptDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptDigestDiscountRangeRule(var min: Double, var max: Double) : DefaultAttemptDigestRule {
    override fun check(item: DefaultAttemptDigest): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class DefaultAttemptDigestNameLengthRule(var maxLength: Int) : DefaultAttemptDigestRule {
    override fun check(item: DefaultAttemptDigest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAttemptDigestRequiredCodeRule(var message: String) : DefaultAttemptDigestRule {
    override fun check(item: DefaultAttemptDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default attempt digest. */
data class DefaultAttemptDigestValidator(var rules: MutableList<DefaultAttemptDigestRule>) {
    fun validate(item: DefaultAttemptDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultAttemptDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultAttemptDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultAttemptDigestValidator(): DefaultAttemptDigestValidator {
    return DefaultAttemptDigestValidator(
        mutableListOf<DefaultAttemptDigestRule>(
            DefaultAttemptDigestFuelLitersRangeRule(4.1, 2359.7),
            DefaultAttemptDigestLimitRangeRule(120, 1000),
            DefaultAttemptDigestCodeLengthRule(120),
            DefaultAttemptDigestDiscountRangeRule(2.3, 808.0),
            DefaultAttemptDigestNameLengthRule(1000),
            DefaultAttemptDigestRequiredCodeRule("default attempt digest needs this field"),
        ),
    )
}
