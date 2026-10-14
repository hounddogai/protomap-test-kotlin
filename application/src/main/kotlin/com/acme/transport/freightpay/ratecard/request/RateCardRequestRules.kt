package com.acme.transport.freightpay.ratecard.request

/** A rule a rate card request must pass. Check returns its problems, or none. */
interface RateCardRequestRule {
    fun check(item: RateCardRequest): MutableList<String>
}

data class RateCardRequestExternalRefLengthRule(var maxLength: Int) : RateCardRequestRule {
    override fun check(item: RateCardRequest): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardRequestCarrierLengthRule(var maxLength: Int) : RateCardRequestRule {
    override fun check(item: RateCardRequest): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardRequestCodePrefixRule(var prefix: String) : RateCardRequestRule {
    override fun check(item: RateCardRequest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RateCardRequestVisibleGuardRule(var limit: Double) : RateCardRequestRule {
    override fun check(item: RateCardRequest): MutableList<String> {
        if (item.visible && item.minimum > limit) {
            return mutableListOf<String>("minimum ${item.minimum} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class RateCardRequestFloorValueRangeRule(var min: Double, var max: Double) : RateCardRequestRule {
    override fun check(item: RateCardRequest): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class RateCardRequestBudgetRangeRule(var min: Double, var max: Double) : RateCardRequestRule {
    override fun check(item: RateCardRequest): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card request. */
data class RateCardRequestValidator(var rules: MutableList<RateCardRequestRule>) {
    fun validate(item: RateCardRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RateCardRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardRequestValidator(): RateCardRequestValidator {
    return RateCardRequestValidator(
        mutableListOf<RateCardRequestRule>(
            RateCardRequestExternalRefLengthRule(16),
            RateCardRequestCarrierLengthRule(3600),
            RateCardRequestCodePrefixRule("Q"),
            RateCardRequestVisibleGuardRule(1.8),
            RateCardRequestFloorValueRangeRule(5.5, 3715.6),
            RateCardRequestBudgetRangeRule(9.0, 608.2),
        ),
    )
}
