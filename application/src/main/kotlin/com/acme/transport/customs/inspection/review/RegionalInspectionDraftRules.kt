package com.acme.transport.customs.inspection.review

/** A rule a regional inspection draft must pass. Check returns its problems, or none. */
interface RegionalInspectionDraftRule {
    fun check(item: RegionalInspectionDraft): MutableList<String>
}

data class RegionalInspectionDraftRequiredVehicleIdRule(var message: String) : RegionalInspectionDraftRule {
    override fun check(item: RegionalInspectionDraft): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionDraftCodePrefixRule(var prefix: String) : RegionalInspectionDraftRule {
    override fun check(item: RegionalInspectionDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionDraftRequiredCurrencyRule(var message: String) : RegionalInspectionDraftRule {
    override fun check(item: RegionalInspectionDraft): MutableList<String> {
        if (item.currency.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionDraftOdometerRangeRule(var min: Int, var max: Int) : RegionalInspectionDraftRule {
    override fun check(item: RegionalInspectionDraft): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class RegionalInspectionDraftRequiredCodeRule(var message: String) : RegionalInspectionDraftRule {
    override fun check(item: RegionalInspectionDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionDraftRequiredTitleRule(var message: String) : RegionalInspectionDraftRule {
    override fun check(item: RegionalInspectionDraft): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional inspection draft. */
data class RegionalInspectionDraftValidator(var rules: MutableList<RegionalInspectionDraftRule>) {
    fun validate(item: RegionalInspectionDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalInspectionDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalInspectionDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalInspectionDraftValidator(): RegionalInspectionDraftValidator {
    return RegionalInspectionDraftValidator(
        mutableListOf<RegionalInspectionDraftRule>(
            RegionalInspectionDraftRequiredVehicleIdRule("regional inspection draft needs a value"),
            RegionalInspectionDraftCodePrefixRule("X"),
            RegionalInspectionDraftRequiredCurrencyRule("regional inspection draft needs a value"),
            RegionalInspectionDraftOdometerRangeRule(32, 3600),
            RegionalInspectionDraftRequiredCodeRule("regional inspection draft needs an entry"),
            RegionalInspectionDraftRequiredTitleRule("regional inspection draft needs a value"),
        ),
    )
}
