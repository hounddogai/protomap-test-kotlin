package com.acme.transport.freightpay.ratecard.reservation

/** A rule a lenient rate card profile must pass. Check returns its problems, or none. */
interface LenientRateCardProfileRule {
    fun check(item: LenientRateCardProfile): MutableList<String>
}

data class LenientRateCardProfileTitleLengthRule(var maxLength: Int) : LenientRateCardProfileRule {
    override fun check(item: LenientRateCardProfile): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardProfilePhoneLengthRule(var maxLength: Int) : LenientRateCardProfileRule {
    override fun check(item: LenientRateCardProfile): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardProfileRequiredCodeRule(var message: String) : LenientRateCardProfileRule {
    override fun check(item: LenientRateCardProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient rate card profile. */
data class LenientRateCardProfileValidator(var rules: MutableList<LenientRateCardProfileRule>) {
    fun validate(item: LenientRateCardProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientRateCardProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRateCardProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRateCardProfileValidator(): LenientRateCardProfileValidator {
    return LenientRateCardProfileValidator(
        mutableListOf<LenientRateCardProfileRule>(
            LenientRateCardProfileTitleLengthRule(120),
            LenientRateCardProfilePhoneLengthRule(86400),
            LenientRateCardProfileRequiredCodeRule("lenient rate card profile needs an entry"),
        ),
    )
}
