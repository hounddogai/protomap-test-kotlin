package com.acme.transport.freightpay.ratecard.request

/** A rule a rate card threshold must pass. Check returns its problems, or none. */
interface RateCardThresholdRule {
    fun check(item: RateCardThreshold): MutableList<String>
}

data class RateCardThresholdRequiredCodeRule(var message: String) : RateCardThresholdRule {
    override fun check(item: RateCardThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RateCardThresholdShiftCodeLengthRule(var maxLength: Int) : RateCardThresholdRule {
    override fun check(item: RateCardThreshold): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardThresholdDepotLengthRule(var maxLength: Int) : RateCardThresholdRule {
    override fun check(item: RateCardThreshold): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardThresholdCodeLengthRule(var maxLength: Int) : RateCardThresholdRule {
    override fun check(item: RateCardThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RateCardThresholdRequiredTitleRule(var message: String) : RateCardThresholdRule {
    override fun check(item: RateCardThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rate card threshold. */
data class RateCardThresholdValidator(var rules: MutableList<RateCardThresholdRule>) {
    fun validate(item: RateCardThreshold): MutableList<String> {
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

    fun requireValid(item: RateCardThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RateCardThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRateCardThresholdValidator(): RateCardThresholdValidator {
    return RateCardThresholdValidator(
        mutableListOf<RateCardThresholdRule>(
            RateCardThresholdRequiredCodeRule("rate card threshold needs a value"),
            RateCardThresholdShiftCodeLengthRule(16),
            RateCardThresholdDepotLengthRule(16),
            RateCardThresholdCodeLengthRule(120),
            RateCardThresholdRequiredTitleRule("rate card threshold needs an entry"),
        ),
    )
}
