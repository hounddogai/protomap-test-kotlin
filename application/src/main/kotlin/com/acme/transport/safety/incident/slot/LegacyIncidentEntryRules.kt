package com.acme.transport.safety.incident.slot

/** A rule a legacy incident entry must pass. Check returns its problems, or none. */
interface LegacyIncidentEntryRule {
    fun check(item: LegacyIncidentEntry): MutableList<String>
}

data class LegacyIncidentEntryRequiredTimezoneRule(var message: String) : LegacyIncidentEntryRule {
    override fun check(item: LegacyIncidentEntry): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentEntryQuantityRangeRule(var min: Int, var max: Int) : LegacyIncidentEntryRule {
    override fun check(item: LegacyIncidentEntry): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentEntryLabelLengthRule(var maxLength: Int) : LegacyIncidentEntryRule {
    override fun check(item: LegacyIncidentEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentEntryRequiredLabelRule(var message: String) : LegacyIncidentEntryRule {
    override fun check(item: LegacyIncidentEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentEntryRequiredCountryRule(var message: String) : LegacyIncidentEntryRule {
    override fun check(item: LegacyIncidentEntry): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentEntryCountryLengthRule(var maxLength: Int) : LegacyIncidentEntryRule {
    override fun check(item: LegacyIncidentEntry): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy incident entry. */
data class LegacyIncidentEntryValidator(var rules: MutableList<LegacyIncidentEntryRule>) {
    fun validate(item: LegacyIncidentEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyIncidentEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyIncidentEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyIncidentEntryValidator(): LegacyIncidentEntryValidator {
    return LegacyIncidentEntryValidator(
        mutableListOf<LegacyIncidentEntryRule>(
            LegacyIncidentEntryRequiredTimezoneRule("legacy incident entry needs a value"),
            LegacyIncidentEntryQuantityRangeRule(16, 255),
            LegacyIncidentEntryLabelLengthRule(80),
            LegacyIncidentEntryRequiredLabelRule("legacy incident entry needs an entry"),
            LegacyIncidentEntryRequiredCountryRule("legacy incident entry needs an entry"),
            LegacyIncidentEntryCountryLengthRule(3600),
        ),
    )
}
