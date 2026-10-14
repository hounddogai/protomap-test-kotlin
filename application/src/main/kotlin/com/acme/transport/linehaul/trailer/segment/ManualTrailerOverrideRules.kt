package com.acme.transport.linehaul.trailer.segment

/** A rule a manual trailer override must pass. Check returns its problems, or none. */
interface ManualTrailerOverrideRule {
    fun check(item: ManualTrailerOverride): MutableList<String>
}

data class ManualTrailerOverrideTitleLengthRule(var maxLength: Int) : ManualTrailerOverrideRule {
    override fun check(item: ManualTrailerOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerOverrideRequiredShiftCodeRule(var message: String) : ManualTrailerOverrideRule {
    override fun check(item: ManualTrailerOverride): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerOverrideCodePrefixRule(var prefix: String) : ManualTrailerOverrideRule {
    override fun check(item: ManualTrailerOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerOverrideUnitLengthRule(var maxLength: Int) : ManualTrailerOverrideRule {
    override fun check(item: ManualTrailerOverride): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerOverrideMaximumRangeRule(var min: Double, var max: Double) : ManualTrailerOverrideRule {
    override fun check(item: ManualTrailerOverride): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

data class ManualTrailerOverrideShiftCodeLengthRule(var maxLength: Int) : ManualTrailerOverrideRule {
    override fun check(item: ManualTrailerOverride): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual trailer override. */
data class ManualTrailerOverrideValidator(var rules: MutableList<ManualTrailerOverrideRule>) {
    fun validate(item: ManualTrailerOverride): MutableList<String> {
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

    fun requireValid(item: ManualTrailerOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTrailerOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTrailerOverrideValidator(): ManualTrailerOverrideValidator {
    return ManualTrailerOverrideValidator(
        mutableListOf<ManualTrailerOverrideRule>(
            ManualTrailerOverrideTitleLengthRule(64),
            ManualTrailerOverrideRequiredShiftCodeRule("manual trailer override needs this field"),
            ManualTrailerOverrideCodePrefixRule("Q"),
            ManualTrailerOverrideUnitLengthRule(1000),
            ManualTrailerOverrideMaximumRangeRule(0.2, 2153.9),
            ManualTrailerOverrideShiftCodeLengthRule(80),
        ),
    )
}
