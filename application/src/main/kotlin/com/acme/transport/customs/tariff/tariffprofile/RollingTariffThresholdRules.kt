package com.acme.transport.customs.tariff.tariffprofile

/** A rule a rolling tariff threshold must pass. Check returns its problems, or none. */
interface RollingTariffThresholdRule {
    fun check(item: RollingTariffThreshold): MutableList<String>
}

data class RollingTariffThresholdWindowDaysRangeRule(var min: Int, var max: Int) : RollingTariffThresholdRule {
    override fun check(item: RollingTariffThreshold): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

data class RollingTariffThresholdGraceDaysRangeRule(var min: Int, var max: Int) : RollingTariffThresholdRule {
    override fun check(item: RollingTariffThreshold): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class RollingTariffThresholdRequiredLabelRule(var message: String) : RollingTariffThresholdRule {
    override fun check(item: RollingTariffThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTariffThresholdRequiredContactEmailRule(var message: String) : RollingTariffThresholdRule {
    override fun check(item: RollingTariffThreshold): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling tariff threshold. */
data class RollingTariffThresholdValidator(var rules: MutableList<RollingTariffThresholdRule>) {
    fun validate(item: RollingTariffThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingTariffThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTariffThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTariffThresholdValidator(): RollingTariffThresholdValidator {
    return RollingTariffThresholdValidator(
        mutableListOf<RollingTariffThresholdRule>(
            RollingTariffThresholdWindowDaysRangeRule(16, 1000),
            RollingTariffThresholdGraceDaysRangeRule(80, 86400),
            RollingTariffThresholdRequiredLabelRule("rolling tariff threshold needs this field"),
            RollingTariffThresholdRequiredContactEmailRule("rolling tariff threshold needs this field"),
        ),
    )
}
