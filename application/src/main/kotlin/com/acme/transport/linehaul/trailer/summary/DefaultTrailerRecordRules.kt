package com.acme.transport.linehaul.trailer.summary

/** A rule a default trailer record must pass. Check returns its problems, or none. */
interface DefaultTrailerRecordRule {
    fun check(item: DefaultTrailerRecord): MutableList<String>
}

data class DefaultTrailerRecordRequiredCodeRule(var message: String) : DefaultTrailerRecordRule {
    override fun check(item: DefaultTrailerRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerRecordVisibleGuardRule(var limit: Double) : DefaultTrailerRecordRule {
    override fun check(item: DefaultTrailerRecord): MutableList<String> {
        if (item.visible && item.weight > limit) {
            return mutableListOf<String>("weight ${item.weight} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerRecordSummaryLengthRule(var maxLength: Int) : DefaultTrailerRecordRule {
    override fun check(item: DefaultTrailerRecord): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerRecordRequiredSummaryRule(var message: String) : DefaultTrailerRecordRule {
    override fun check(item: DefaultTrailerRecord): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultTrailerRecordEndsAtRangeRule(var min: Int, var max: Int) : DefaultTrailerRecordRule {
    override fun check(item: DefaultTrailerRecord): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class DefaultTrailerRecordWeightRangeRule(var min: Double, var max: Double) : DefaultTrailerRecordRule {
    override fun check(item: DefaultTrailerRecord): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a default trailer record. */
data class DefaultTrailerRecordValidator(var rules: MutableList<DefaultTrailerRecordRule>) {
    fun validate(item: DefaultTrailerRecord): MutableList<String> {
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

    fun requireValid(item: DefaultTrailerRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultTrailerRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultTrailerRecordValidator(): DefaultTrailerRecordValidator {
    return DefaultTrailerRecordValidator(
        mutableListOf<DefaultTrailerRecordRule>(
            DefaultTrailerRecordRequiredCodeRule("default trailer record needs this field"),
            DefaultTrailerRecordVisibleGuardRule(6.6),
            DefaultTrailerRecordSummaryLengthRule(120),
            DefaultTrailerRecordRequiredSummaryRule("default trailer record needs a value"),
            DefaultTrailerRecordEndsAtRangeRule(16, 32),
            DefaultTrailerRecordWeightRangeRule(7.0, 3535.6),
        ),
    )
}
