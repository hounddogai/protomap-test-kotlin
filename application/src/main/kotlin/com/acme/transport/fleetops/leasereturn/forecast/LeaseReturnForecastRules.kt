package com.acme.transport.fleetops.leasereturn.forecast

/** A rule a lease return forecast must pass. Check returns its problems, or none. */
interface LeaseReturnForecastRule {
    fun check(item: LeaseReturnForecast): MutableList<String>
}

data class LeaseReturnForecastLoadKgRangeRule(var min: Double, var max: Double) : LeaseReturnForecastRule {
    override fun check(item: LeaseReturnForecast): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnForecastCreatedAtRangeRule(var min: Int, var max: Int) : LeaseReturnForecastRule {
    override fun check(item: LeaseReturnForecast): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnForecastShiftCodeLengthRule(var maxLength: Int) : LeaseReturnForecastRule {
    override fun check(item: LeaseReturnForecast): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnForecastRequiredTitleRule(var message: String) : LeaseReturnForecastRule {
    override fun check(item: LeaseReturnForecast): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LeaseReturnForecastRequiredCodeRule(var message: String) : LeaseReturnForecastRule {
    override fun check(item: LeaseReturnForecast): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lease return forecast. */
data class LeaseReturnForecastValidator(var rules: MutableList<LeaseReturnForecastRule>) {
    fun validate(item: LeaseReturnForecast): MutableList<String> {
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

    fun requireValid(item: LeaseReturnForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LeaseReturnForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultLeaseReturnForecastValidator(): LeaseReturnForecastValidator {
    return LeaseReturnForecastValidator(
        mutableListOf<LeaseReturnForecastRule>(
            LeaseReturnForecastLoadKgRangeRule(4.1, 4317.8),
            LeaseReturnForecastCreatedAtRangeRule(120, 1000),
            LeaseReturnForecastShiftCodeLengthRule(80),
            LeaseReturnForecastRequiredTitleRule("lease return forecast needs an entry"),
            LeaseReturnForecastRequiredCodeRule("lease return forecast needs this field"),
        ),
    )
}
