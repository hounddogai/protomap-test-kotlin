package com.acme.transport.terminal.crossdock.baseline

/** A rule a lenient cross dock baseline must pass. Check returns its problems, or none. */
interface LenientCrossDockBaselineRule {
    fun check(item: LenientCrossDockBaseline): MutableList<String>
}

data class LenientCrossDockBaselineRequiredLaneRule(var message: String) : LenientCrossDockBaselineRule {
    override fun check(item: LenientCrossDockBaseline): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockBaselineRequiredNameRule(var message: String) : LenientCrossDockBaselineRule {
    override fun check(item: LenientCrossDockBaseline): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockBaselinePhoneLengthRule(var maxLength: Int) : LenientCrossDockBaselineRule {
    override fun check(item: LenientCrossDockBaseline): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockBaselineCodePrefixRule(var prefix: String) : LenientCrossDockBaselineRule {
    override fun check(item: LenientCrossDockBaseline): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockBaselineMarginRangeRule(
    var min: Double,
    var max: Double,
) : LenientCrossDockBaselineRule {
    override fun check(item: LenientCrossDockBaseline): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientCrossDockBaselineLaneLengthRule(var maxLength: Int) : LenientCrossDockBaselineRule {
    override fun check(item: LenientCrossDockBaseline): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient cross dock baseline. */
data class LenientCrossDockBaselineValidator(var rules: MutableList<LenientCrossDockBaselineRule>) {
    fun validate(item: LenientCrossDockBaseline): MutableList<String> {
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

    fun requireValid(item: LenientCrossDockBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCrossDockBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCrossDockBaselineValidator(): LenientCrossDockBaselineValidator {
    return LenientCrossDockBaselineValidator(
        mutableListOf<LenientCrossDockBaselineRule>(
            LenientCrossDockBaselineRequiredLaneRule("lenient cross dock baseline needs a value"),
            LenientCrossDockBaselineRequiredNameRule("lenient cross dock baseline needs this field"),
            LenientCrossDockBaselinePhoneLengthRule(86400),
            LenientCrossDockBaselineCodePrefixRule("Q"),
            LenientCrossDockBaselineMarginRangeRule(0.5, 2082.7),
            LenientCrossDockBaselineLaneLengthRule(64),
        ),
    )
}
