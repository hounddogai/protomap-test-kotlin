package com.acme.transport.fleetops.serviceplan.legacyplanrollup

/** A rule a manual service plan forecast must pass. Check returns its problems, or none. */
interface ManualServicePlanForecastRule {
    fun check(item: ManualServicePlanForecast): MutableList<String>
}

data class ManualServicePlanForecastApprovedGuardRule(var limit: Int) : ManualServicePlanForecastRule {
    override fun check(item: ManualServicePlanForecast): MutableList<String> {
        if (item.approved && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanForecastRequiredShiftCodeRule(var message: String) : ManualServicePlanForecastRule {
    override fun check(item: ManualServicePlanForecast): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanForecastRequiredLabelRule(var message: String) : ManualServicePlanForecastRule {
    override fun check(item: ManualServicePlanForecast): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanForecastCodePrefixRule(var prefix: String) : ManualServicePlanForecastRule {
    override fun check(item: ManualServicePlanForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanForecastRequiredCodeRule(var message: String) : ManualServicePlanForecastRule {
    override fun check(item: ManualServicePlanForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual service plan forecast. */
data class ManualServicePlanForecastValidator(var rules: MutableList<ManualServicePlanForecastRule>) {
    fun validate(item: ManualServicePlanForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualServicePlanForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualServicePlanForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultManualServicePlanForecastValidator(): ManualServicePlanForecastValidator {
    return ManualServicePlanForecastValidator(
        mutableListOf<ManualServicePlanForecastRule>(
            ManualServicePlanForecastApprovedGuardRule(80),
            ManualServicePlanForecastRequiredShiftCodeRule("manual service plan forecast needs an entry"),
            ManualServicePlanForecastRequiredLabelRule("manual service plan forecast needs this field"),
            ManualServicePlanForecastCodePrefixRule("REF"),
            ManualServicePlanForecastRequiredCodeRule("manual service plan forecast needs this field"),
        ),
    )
}
