package com.acme.transport.fleetops.leasereturn.mapping

/** A rule a manual lease return forecast must pass. Check returns its problems, or none. */
interface ManualLeaseReturnForecastRule {
    fun check(item: ManualLeaseReturnForecast): MutableList<String>
}

data class ManualLeaseReturnForecastAttemptsRangeRule(
    var min: Int,
    var max: Int,
) : ManualLeaseReturnForecastRule {
    override fun check(item: ManualLeaseReturnForecast): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualLeaseReturnForecastEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ManualLeaseReturnForecastRule {
    override fun check(item: ManualLeaseReturnForecast): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ManualLeaseReturnForecastRequiredCodeRule(var message: String) : ManualLeaseReturnForecastRule {
    override fun check(item: ManualLeaseReturnForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual lease return forecast. */
data class ManualLeaseReturnForecastValidator(var rules: MutableList<ManualLeaseReturnForecastRule>) {
    fun validate(item: ManualLeaseReturnForecast): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualLeaseReturnForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualLeaseReturnForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultManualLeaseReturnForecastValidator(): ManualLeaseReturnForecastValidator {
    return ManualLeaseReturnForecastValidator(
        mutableListOf<ManualLeaseReturnForecastRule>(
            ManualLeaseReturnForecastAttemptsRangeRule(1000, 1000),
            ManualLeaseReturnForecastEtaMinutesRangeRule(32, 120),
            ManualLeaseReturnForecastRequiredCodeRule("manual lease return forecast needs this field"),
        ),
    )
}
