package com.acme.transport.linehaul.trailer.snapshot

/** A rule a trailer profile must pass. Check returns its problems, or none. */
interface TrailerProfileRule {
    fun check(item: TrailerProfile): MutableList<String>
}

data class TrailerProfileGraceDaysRangeRule(var min: Int, var max: Int) : TrailerProfileRule {
    override fun check(item: TrailerProfile): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class TrailerProfilePhoneLengthRule(var maxLength: Int) : TrailerProfileRule {
    override fun check(item: TrailerProfile): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerProfileCodePrefixRule(var prefix: String) : TrailerProfileRule {
    override fun check(item: TrailerProfile): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TrailerProfileCodeLengthRule(var maxLength: Int) : TrailerProfileRule {
    override fun check(item: TrailerProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer profile. */
data class TrailerProfileValidator(var rules: MutableList<TrailerProfileRule>) {
    fun validate(item: TrailerProfile): MutableList<String> {
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

    fun requireValid(item: TrailerProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerProfileValidator(): TrailerProfileValidator {
    return TrailerProfileValidator(
        mutableListOf<TrailerProfileRule>(
            TrailerProfileGraceDaysRangeRule(120, 3600),
            TrailerProfilePhoneLengthRule(32),
            TrailerProfileCodePrefixRule("ACM"),
            TrailerProfileCodeLengthRule(120),
        ),
    )
}
