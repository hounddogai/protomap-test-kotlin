package com.acme.transport.freightpay.ratecard.rule

/** A rule a rate card profile must pass. Check returns its problems, or none. */
interface RateCardProfileRule {
    fun check(item: RateCardProfile): MutableList<String>
}

data class RateCardProfileRequiredCodeRule(var message: String) : RateCardProfileRule {
    override fun check(item: RateCardProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardProfileCodePrefixRule(var prefix: String) : RateCardProfileRule {
    override fun check(item: RateCardProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardProfilePinnedGuardRule(var limit: Double) : RateCardProfileRule {
    override fun check(item: RateCardProfile): MutableList<String> {
        if (item.pinned && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class RateCardProfileRequiredLabelRule(var message: String) : RateCardProfileRule {
    override fun check(item: RateCardProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardProfileLabelLengthRule(var maxLength: Int) : RateCardProfileRule {
    override fun check(item: RateCardProfile): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card profile. */
data class RateCardProfileValidator(var rules: MutableList<RateCardProfileRule>) {
    fun validate(item: RateCardProfile): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardProfileValidator(): RateCardProfileValidator {
    return RateCardProfileValidator(
        mutableListOf<RateCardProfileRule>(
            RateCardProfileRequiredCodeRule("rate card profile needs this field"),
            RateCardProfileCodePrefixRule("ACM"),
            RateCardProfilePinnedGuardRule(0.4),
            RateCardProfileRequiredLabelRule("rate card profile needs an entry"),
            RateCardProfileLabelLengthRule(80),
        ),
    )
}
