package com.acme.transport.lastmile.parcel.review

/** A rule a legacy parcel threshold must pass. Check returns its problems, or none. */
interface LegacyParcelThresholdRule {
    fun check(item: LegacyParcelThreshold): MutableList<String>
}

data class LegacyParcelThresholdRequiredCodeRule(var message: String) : LegacyParcelThresholdRule {
    override fun check(item: LegacyParcelThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyParcelThresholdLabelLengthRule(var maxLength: Int) : LegacyParcelThresholdRule {
    override fun check(item: LegacyParcelThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyParcelThresholdExpiresAtRangeRule(var min: Int, var max: Int) : LegacyParcelThresholdRule {
    override fun check(item: LegacyParcelThreshold): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy parcel threshold. */
data class LegacyParcelThresholdValidator(var rules: MutableList<LegacyParcelThresholdRule>) {
    fun validate(item: LegacyParcelThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyParcelThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyParcelThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyParcelThresholdValidator(): LegacyParcelThresholdValidator {
    return LegacyParcelThresholdValidator(
        mutableListOf<LegacyParcelThresholdRule>(
            LegacyParcelThresholdRequiredCodeRule("legacy parcel threshold needs an entry"),
            LegacyParcelThresholdLabelLengthRule(64),
            LegacyParcelThresholdExpiresAtRangeRule(64, 80),
        ),
    )
}
