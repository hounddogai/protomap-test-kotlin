package com.acme.transport.fleetops.defect.defectforecast

/** A rule a legacy defect forecast must pass. Check returns its problems, or none. */
interface LegacyDefectForecastRule {
    fun check(item: LegacyDefectForecast): MutableList<String>
}

data class LegacyDefectForecastRequiredTitleRule(var message: String) : LegacyDefectForecastRule {
    override fun check(item: LegacyDefectForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectForecastPinnedGuardRule(var limit: Double) : LegacyDefectForecastRule {
    override fun check(item: LegacyDefectForecast): MutableList<String> {
        if (item.pinned && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectForecastRequiredRouteCodeRule(var message: String) : LegacyDefectForecastRule {
    override fun check(item: LegacyDefectForecast): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectForecastCodePrefixRule(var prefix: String) : LegacyDefectForecastRule {
    override fun check(item: LegacyDefectForecast): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDefectForecastCodeLengthRule(var maxLength: Int) : LegacyDefectForecastRule {
    override fun check(item: LegacyDefectForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy defect forecast. */
data class LegacyDefectForecastValidator(var rules: MutableList<LegacyDefectForecastRule>) {
    fun validate(item: LegacyDefectForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyDefectForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDefectForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDefectForecastValidator(): LegacyDefectForecastValidator {
    return LegacyDefectForecastValidator(
        mutableListOf<LegacyDefectForecastRule>(
            LegacyDefectForecastRequiredTitleRule("legacy defect forecast needs this field"),
            LegacyDefectForecastPinnedGuardRule(6.2),
            LegacyDefectForecastRequiredRouteCodeRule("legacy defect forecast needs this field"),
            LegacyDefectForecastCodePrefixRule("ACM"),
            LegacyDefectForecastCodeLengthRule(86400),
        ),
    )
}
