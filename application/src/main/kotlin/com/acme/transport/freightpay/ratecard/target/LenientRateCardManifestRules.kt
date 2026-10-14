package com.acme.transport.freightpay.ratecard.target

/** A rule a lenient rate card manifest must pass. Check returns its problems, or none. */
interface LenientRateCardManifestRule {
    fun check(item: LenientRateCardManifest): MutableList<String>
}

data class LenientRateCardManifestRequiredChannelRule(var message: String) : LenientRateCardManifestRule {
    override fun check(item: LenientRateCardManifest): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardManifestRequiredCodeRule(var message: String) : LenientRateCardManifestRule {
    override fun check(item: LenientRateCardManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardManifestAttemptsRangeRule(var min: Int, var max: Int) : LenientRateCardManifestRule {
    override fun check(item: LenientRateCardManifest): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientRateCardManifestRequiredDescriptionRule(var message: String) : LenientRateCardManifestRule {
    override fun check(item: LenientRateCardManifest): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient rate card manifest. */
data class LenientRateCardManifestValidator(var rules: MutableList<LenientRateCardManifestRule>) {
    fun validate(item: LenientRateCardManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientRateCardManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRateCardManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRateCardManifestValidator(): LenientRateCardManifestValidator {
    return LenientRateCardManifestValidator(
        mutableListOf<LenientRateCardManifestRule>(
            LenientRateCardManifestRequiredChannelRule("lenient rate card manifest needs an entry"),
            LenientRateCardManifestRequiredCodeRule("lenient rate card manifest needs this field"),
            LenientRateCardManifestAttemptsRangeRule(32, 255),
            LenientRateCardManifestRequiredDescriptionRule("lenient rate card manifest needs this field"),
        ),
    )
}
