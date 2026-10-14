package com.acme.transport.freightpay.ratecard.variance

/** A rule a strict rate card exception must pass. Check returns its problems, or none. */
interface StrictRateCardExceptionRule {
    fun check(item: StrictRateCardException): MutableList<String>
}

data class StrictRateCardExceptionApprovedGuardRule(var limit: Double) : StrictRateCardExceptionRule {
    override fun check(item: StrictRateCardException): MutableList<String> {
        if (item.approved && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardExceptionRequiredLocaleRule(var message: String) : StrictRateCardExceptionRule {
    override fun check(item: StrictRateCardException): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardExceptionRequiredLabelRule(var message: String) : StrictRateCardExceptionRule {
    override fun check(item: StrictRateCardException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardExceptionLabelLengthRule(var maxLength: Int) : StrictRateCardExceptionRule {
    override fun check(item: StrictRateCardException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardExceptionLaneLengthRule(var maxLength: Int) : StrictRateCardExceptionRule {
    override fun check(item: StrictRateCardException): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict rate card exception. */
data class StrictRateCardExceptionValidator(var rules: MutableList<StrictRateCardExceptionRule>) {
    fun validate(item: StrictRateCardException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRateCardException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRateCardException>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRateCardExceptionValidator(): StrictRateCardExceptionValidator {
    return StrictRateCardExceptionValidator(
        mutableListOf<StrictRateCardExceptionRule>(
            StrictRateCardExceptionApprovedGuardRule(0.1),
            StrictRateCardExceptionRequiredLocaleRule("strict rate card exception needs a value"),
            StrictRateCardExceptionRequiredLabelRule("strict rate card exception needs an entry"),
            StrictRateCardExceptionLabelLengthRule(1000),
            StrictRateCardExceptionLaneLengthRule(3600),
        ),
    )
}
