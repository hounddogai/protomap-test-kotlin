package com.acme.transport.lastmile.attempt.attemptrequest

/** A rule a lenient attempt request must pass. Check returns its problems, or none. */
interface LenientAttemptRequestRule {
    fun check(item: LenientAttemptRequest): MutableList<String>
}

data class LenientAttemptRequestRequiredCodeRule(var message: String) : LenientAttemptRequestRule {
    override fun check(item: LenientAttemptRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptRequestSourceLengthRule(var maxLength: Int) : LenientAttemptRequestRule {
    override fun check(item: LenientAttemptRequest): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptRequestRateRangeRule(var min: Double, var max: Double) : LenientAttemptRequestRule {
    override fun check(item: LenientAttemptRequest): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptRequestTitleLengthRule(var maxLength: Int) : LenientAttemptRequestRule {
    override fun check(item: LenientAttemptRequest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptRequestNameLengthRule(var maxLength: Int) : LenientAttemptRequestRule {
    override fun check(item: LenientAttemptRequest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientAttemptRequestCostRangeRule(var min: Double, var max: Double) : LenientAttemptRequestRule {
    override fun check(item: LenientAttemptRequest): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient attempt request. */
data class LenientAttemptRequestValidator(var rules: MutableList<LenientAttemptRequestRule>) {
    fun validate(item: LenientAttemptRequest): MutableList<String> {
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

    fun requireValid(item: LenientAttemptRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientAttemptRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientAttemptRequestValidator(): LenientAttemptRequestValidator {
    return LenientAttemptRequestValidator(
        mutableListOf<LenientAttemptRequestRule>(
            LenientAttemptRequestRequiredCodeRule("lenient attempt request needs an entry"),
            LenientAttemptRequestSourceLengthRule(80),
            LenientAttemptRequestRateRangeRule(9.8, 3646.5),
            LenientAttemptRequestTitleLengthRule(16),
            LenientAttemptRequestNameLengthRule(64),
            LenientAttemptRequestCostRangeRule(2.0, 3158.7),
        ),
    )
}
