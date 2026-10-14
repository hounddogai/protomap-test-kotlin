package com.acme.transport.linehaul.tender.preference

/** A rule a legacy tender preference must pass. Check returns its problems, or none. */
interface LegacyTenderPreferenceRule {
    fun check(item: LegacyTenderPreference): MutableList<String>
}

data class LegacyTenderPreferenceCodeLengthRule(var maxLength: Int) : LegacyTenderPreferenceRule {
    override fun check(item: LegacyTenderPreference): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderPreferenceRequiredCodeRule(var message: String) : LegacyTenderPreferenceRule {
    override fun check(item: LegacyTenderPreference): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderPreferenceCategoryLengthRule(var maxLength: Int) : LegacyTenderPreferenceRule {
    override fun check(item: LegacyTenderPreference): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTenderPreferenceRequiredUnitRule(var message: String) : LegacyTenderPreferenceRule {
    override fun check(item: LegacyTenderPreference): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy tender preference. */
data class LegacyTenderPreferenceValidator(var rules: MutableList<LegacyTenderPreferenceRule>) {
    fun validate(item: LegacyTenderPreference): MutableList<String> {
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

    fun requireValid(item: LegacyTenderPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTenderPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTenderPreferenceValidator(): LegacyTenderPreferenceValidator {
    return LegacyTenderPreferenceValidator(
        mutableListOf<LegacyTenderPreferenceRule>(
            LegacyTenderPreferenceCodeLengthRule(1000),
            LegacyTenderPreferenceRequiredCodeRule("legacy tender preference needs this field"),
            LegacyTenderPreferenceCategoryLengthRule(120),
            LegacyTenderPreferenceRequiredUnitRule("legacy tender preference needs an entry"),
        ),
    )
}
