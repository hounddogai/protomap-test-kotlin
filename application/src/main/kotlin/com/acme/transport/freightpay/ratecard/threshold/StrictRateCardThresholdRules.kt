package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.kit.text.normalize

/** A rule a strict rate card threshold must pass. Check returns its problems, or none. */
interface StrictRateCardThresholdRule {
    fun check(item: StrictRateCardThreshold): MutableList<String>
}

data class StrictRateCardThresholdAllowedDockRule(
    var allowed: MutableList<String>,
) : StrictRateCardThresholdRule {
    override fun check(item: StrictRateCardThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.dock))) {
            return mutableListOf<String>("dock ${item.dock} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardThresholdRequiredDriverIdRule(var message: String) : StrictRateCardThresholdRule {
    override fun check(item: StrictRateCardThreshold): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRateCardThresholdLabelLengthRule(var maxLength: Int) : StrictRateCardThresholdRule {
    override fun check(item: StrictRateCardThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict rate card threshold. */
data class StrictRateCardThresholdValidator(var rules: MutableList<StrictRateCardThresholdRule>) {
    fun validate(item: StrictRateCardThreshold): MutableList<String> {
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

    fun requireValid(item: StrictRateCardThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRateCardThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRateCardThresholdValidator(): StrictRateCardThresholdValidator {
    return StrictRateCardThresholdValidator(
        mutableListOf<StrictRateCardThresholdRule>(
            StrictRateCardThresholdAllowedDockRule(mutableListOf<String>("regional", "fallback")),
            StrictRateCardThresholdRequiredDriverIdRule("strict rate card threshold needs an entry"),
            StrictRateCardThresholdLabelLengthRule(1000),
        ),
    )
}
