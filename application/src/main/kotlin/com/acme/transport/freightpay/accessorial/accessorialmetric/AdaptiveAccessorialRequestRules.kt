package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.text.normalize

/** A rule a adaptive accessorial request must pass. Check returns its problems, or none. */
interface AdaptiveAccessorialRequestRule {
    fun check(item: AdaptiveAccessorialRequest): MutableList<String>
}

data class AdaptiveAccessorialRequestRequiredCodeRule(var message: String) : AdaptiveAccessorialRequestRule {
    override fun check(item: AdaptiveAccessorialRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAccessorialRequestScoreRangeRule(
    var min: Double,
    var max: Double,
) : AdaptiveAccessorialRequestRule {
    override fun check(item: AdaptiveAccessorialRequest): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class AdaptiveAccessorialRequestLabelLengthRule(var maxLength: Int) : AdaptiveAccessorialRequestRule {
    override fun check(item: AdaptiveAccessorialRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAccessorialRequestAllowedLabelRule(
    var allowed: MutableList<String>,
) : AdaptiveAccessorialRequestRule {
    override fun check(item: AdaptiveAccessorialRequest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is too long")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveAccessorialRequestCodeLengthRule(var maxLength: Int) : AdaptiveAccessorialRequestRule {
    override fun check(item: AdaptiveAccessorialRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive accessorial request. */
data class AdaptiveAccessorialRequestValidator(var rules: MutableList<AdaptiveAccessorialRequestRule>) {
    fun validate(item: AdaptiveAccessorialRequest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AdaptiveAccessorialRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveAccessorialRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveAccessorialRequestValidator(): AdaptiveAccessorialRequestValidator {
    return AdaptiveAccessorialRequestValidator(
        mutableListOf<AdaptiveAccessorialRequestRule>(
            AdaptiveAccessorialRequestRequiredCodeRule("adaptive accessorial request needs a value"),
            AdaptiveAccessorialRequestScoreRangeRule(9.0, 1698.5),
            AdaptiveAccessorialRequestLabelLengthRule(1000),
            AdaptiveAccessorialRequestAllowedLabelRule(mutableListOf<String>()),
            AdaptiveAccessorialRequestCodeLengthRule(64),
        ),
    )
}
