package com.acme.transport.linehaul.trailer.trailerpreference

/** A rule a capped trailer threshold must pass. Check returns its problems, or none. */
interface CappedTrailerThresholdRule {
    fun check(item: CappedTrailerThreshold): MutableList<String>
}

data class CappedTrailerThresholdPhoneLengthRule(var maxLength: Int) : CappedTrailerThresholdRule {
    override fun check(item: CappedTrailerThreshold): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerThresholdAttemptsRangeRule(var min: Int, var max: Int) : CappedTrailerThresholdRule {
    override fun check(item: CappedTrailerThreshold): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class CappedTrailerThresholdGraceDaysRangeRule(var min: Int, var max: Int) : CappedTrailerThresholdRule {
    override fun check(item: CappedTrailerThreshold): MutableList<String> {
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

/** Runs every rule of a capped trailer threshold. */
data class CappedTrailerThresholdValidator(var rules: MutableList<CappedTrailerThresholdRule>) {
    fun validate(item: CappedTrailerThreshold): MutableList<String> {
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

    fun requireValid(item: CappedTrailerThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTrailerThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTrailerThresholdValidator(): CappedTrailerThresholdValidator {
    return CappedTrailerThresholdValidator(
        mutableListOf<CappedTrailerThresholdRule>(
            CappedTrailerThresholdPhoneLengthRule(32),
            CappedTrailerThresholdAttemptsRangeRule(255, 255),
            CappedTrailerThresholdGraceDaysRangeRule(80, 1000),
        ),
    )
}
