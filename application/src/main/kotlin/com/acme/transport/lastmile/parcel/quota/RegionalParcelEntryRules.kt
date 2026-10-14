package com.acme.transport.lastmile.parcel.quota

/** A rule a regional parcel entry must pass. Check returns its problems, or none. */
interface RegionalParcelEntryRule {
    fun check(item: RegionalParcelEntry): MutableList<String>
}

data class RegionalParcelEntryRequiredContactEmailRule(var message: String) : RegionalParcelEntryRule {
    override fun check(item: RegionalParcelEntry): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelEntryRequiredLabelRule(var message: String) : RegionalParcelEntryRule {
    override fun check(item: RegionalParcelEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalParcelEntryStartsAtRangeRule(var min: Int, var max: Int) : RegionalParcelEntryRule {
    override fun check(item: RegionalParcelEntry): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a regional parcel entry. */
data class RegionalParcelEntryValidator(var rules: MutableList<RegionalParcelEntryRule>) {
    fun validate(item: RegionalParcelEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalParcelEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalParcelEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalParcelEntryValidator(): RegionalParcelEntryValidator {
    return RegionalParcelEntryValidator(
        mutableListOf<RegionalParcelEntryRule>(
            RegionalParcelEntryRequiredContactEmailRule("regional parcel entry needs an entry"),
            RegionalParcelEntryRequiredLabelRule("regional parcel entry needs this field"),
            RegionalParcelEntryStartsAtRangeRule(80, 3600),
        ),
    )
}
