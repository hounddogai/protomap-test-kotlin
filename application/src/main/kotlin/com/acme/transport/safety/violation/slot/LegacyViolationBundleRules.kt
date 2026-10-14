package com.acme.transport.safety.violation.slot

/** A rule a legacy violation bundle must pass. Check returns its problems, or none. */
interface LegacyViolationBundleRule {
    fun check(item: LegacyViolationBundle): MutableList<String>
}

data class LegacyViolationBundleRequiredUnitRule(var message: String) : LegacyViolationBundleRule {
    override fun check(item: LegacyViolationBundle): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyViolationBundleCodeLengthRule(var maxLength: Int) : LegacyViolationBundleRule {
    override fun check(item: LegacyViolationBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyViolationBundleLabelLengthRule(var maxLength: Int) : LegacyViolationBundleRule {
    override fun check(item: LegacyViolationBundle): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy violation bundle. */
data class LegacyViolationBundleValidator(var rules: MutableList<LegacyViolationBundleRule>) {
    fun validate(item: LegacyViolationBundle): MutableList<String> {
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

    fun requireValid(item: LegacyViolationBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyViolationBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyViolationBundleValidator(): LegacyViolationBundleValidator {
    return LegacyViolationBundleValidator(
        mutableListOf<LegacyViolationBundleRule>(
            LegacyViolationBundleRequiredUnitRule("legacy violation bundle needs this field"),
            LegacyViolationBundleCodeLengthRule(80),
            LegacyViolationBundleLabelLengthRule(80),
        ),
    )
}
