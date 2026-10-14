package com.acme.transport.linehaul.laneplan.target

/** A rule a legacy lane plan entry must pass. Check returns its problems, or none. */
interface LegacyLanePlanEntryRule {
    fun check(item: LegacyLanePlanEntry): MutableList<String>
}

data class LegacyLanePlanEntryLabelLengthRule(var maxLength: Int) : LegacyLanePlanEntryRule {
    override fun check(item: LegacyLanePlanEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanEntryCodeLengthRule(var maxLength: Int) : LegacyLanePlanEntryRule {
    override fun check(item: LegacyLanePlanEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyLanePlanEntryExpiresAtRangeRule(var min: Int, var max: Int) : LegacyLanePlanEntryRule {
    override fun check(item: LegacyLanePlanEntry): MutableList<String> {
        val value = item.expiresAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("expires at $value is below $min")
        }
        if (value > max) {
            problems.add("expires at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy lane plan entry. */
data class LegacyLanePlanEntryValidator(var rules: MutableList<LegacyLanePlanEntryRule>) {
    fun validate(item: LegacyLanePlanEntry): MutableList<String> {
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

    fun requireValid(item: LegacyLanePlanEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyLanePlanEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyLanePlanEntryValidator(): LegacyLanePlanEntryValidator {
    return LegacyLanePlanEntryValidator(
        mutableListOf<LegacyLanePlanEntryRule>(
            LegacyLanePlanEntryLabelLengthRule(86400),
            LegacyLanePlanEntryCodeLengthRule(3600),
            LegacyLanePlanEntryExpiresAtRangeRule(64, 255),
        ),
    )
}
