package com.acme.transport.lastmile.locker.slot

/** A rule a locker metric must pass. Check returns its problems, or none. */
interface LockerMetricRule {
    fun check(item: LockerMetric): MutableList<String>
}

data class LockerMetricGraceDaysRangeRule(var min: Int, var max: Int) : LockerMetricRule {
    override fun check(item: LockerMetric): MutableList<String> {
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

data class LockerMetricRateRangeRule(var min: Double, var max: Double) : LockerMetricRule {
    override fun check(item: LockerMetric): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class LockerMetricStartsAtRangeRule(var min: Int, var max: Int) : LockerMetricRule {
    override fun check(item: LockerMetric): MutableList<String> {
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

data class LockerMetricRequiredCodeRule(var message: String) : LockerMetricRule {
    override fun check(item: LockerMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerMetricRequiredTitleRule(var message: String) : LockerMetricRule {
    override fun check(item: LockerMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker metric. */
data class LockerMetricValidator(var rules: MutableList<LockerMetricRule>) {
    fun validate(item: LockerMetric): MutableList<String> {
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

    fun requireValid(item: LockerMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerMetricValidator(): LockerMetricValidator {
    return LockerMetricValidator(
        mutableListOf<LockerMetricRule>(
            LockerMetricGraceDaysRangeRule(255, 255),
            LockerMetricRateRangeRule(6.2, 3473.4),
            LockerMetricStartsAtRangeRule(86400, 86400),
            LockerMetricRequiredCodeRule("locker metric needs a value"),
            LockerMetricRequiredTitleRule("locker metric needs this field"),
        ),
    )
}
