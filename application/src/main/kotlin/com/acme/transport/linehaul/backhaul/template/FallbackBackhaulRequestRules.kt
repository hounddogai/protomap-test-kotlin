package com.acme.transport.linehaul.backhaul.template

/** A rule a fallback backhaul request must pass. Check returns its problems, or none. */
interface FallbackBackhaulRequestRule {
    fun check(item: FallbackBackhaulRequest): MutableList<String>
}

data class FallbackBackhaulRequestRequiredContactEmailRule(var message: String) : FallbackBackhaulRequestRule {
    override fun check(item: FallbackBackhaulRequest): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulRequestChannelLengthRule(var maxLength: Int) : FallbackBackhaulRequestRule {
    override fun check(item: FallbackBackhaulRequest): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackBackhaulRequestWindowDaysRangeRule(
    var min: Int,
    var max: Int,
) : FallbackBackhaulRequestRule {
    override fun check(item: FallbackBackhaulRequest): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback backhaul request. */
data class FallbackBackhaulRequestValidator(var rules: MutableList<FallbackBackhaulRequestRule>) {
    fun validate(item: FallbackBackhaulRequest): MutableList<String> {
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

    fun requireValid(item: FallbackBackhaulRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackBackhaulRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackBackhaulRequestValidator(): FallbackBackhaulRequestValidator {
    return FallbackBackhaulRequestValidator(
        mutableListOf<FallbackBackhaulRequestRule>(
            FallbackBackhaulRequestRequiredContactEmailRule("fallback backhaul request needs this field"),
            FallbackBackhaulRequestChannelLengthRule(86400),
            FallbackBackhaulRequestWindowDaysRangeRule(1000, 86400),
        ),
    )
}
