package com.acme.transport.freightpay.ratecard.segment

/** A rule a rate card manifest must pass. Check returns its problems, or none. */
interface RateCardManifestRule {
    fun check(item: RateCardManifest): MutableList<String>
}

data class RateCardManifestRequiredCodeRule(var message: String) : RateCardManifestRule {
    override fun check(item: RateCardManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardManifestAmountRangeRule(var min: Double, var max: Double) : RateCardManifestRule {
    override fun check(item: RateCardManifest): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class RateCardManifestCarrierLengthRule(var maxLength: Int) : RateCardManifestRule {
    override fun check(item: RateCardManifest): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardManifestExpiresAtRangeRule(var min: Int, var max: Int) : RateCardManifestRule {
    override fun check(item: RateCardManifest): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

data class RateCardManifestStartsAtRangeRule(var min: Int, var max: Int) : RateCardManifestRule {
    override fun check(item: RateCardManifest): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card manifest. */
data class RateCardManifestValidator(var rules: MutableList<RateCardManifestRule>) {
    fun validate(item: RateCardManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardManifestValidator(): RateCardManifestValidator {
    return RateCardManifestValidator(
        mutableListOf<RateCardManifestRule>(
            RateCardManifestRequiredCodeRule("rate card manifest needs a value"),
            RateCardManifestAmountRangeRule(1.1, 3907.4),
            RateCardManifestCarrierLengthRule(255),
            RateCardManifestExpiresAtRangeRule(80, 80),
            RateCardManifestStartsAtRangeRule(16, 120),
        ),
    )
}
