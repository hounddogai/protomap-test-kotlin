package com.acme.transport.lastmile.locker.lockerrule

/** A rule a tiered locker metric must pass. Check returns its problems, or none. */
interface TieredLockerMetricRule {
    fun check(item: TieredLockerMetric): MutableList<String>
}

data class TieredLockerMetricRequiredDepotRule(var message: String) : TieredLockerMetricRule {
    override fun check(item: TieredLockerMetric): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredLockerMetricRequiredContactEmailRule(var message: String) : TieredLockerMetricRule {
    override fun check(item: TieredLockerMetric): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredLockerMetricDepotLengthRule(var maxLength: Int) : TieredLockerMetricRule {
    override fun check(item: TieredLockerMetric): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered locker metric. */
data class TieredLockerMetricValidator(var rules: MutableList<TieredLockerMetricRule>) {
    fun validate(item: TieredLockerMetric): MutableList<String> {
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

    fun requireValid(item: TieredLockerMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredLockerMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredLockerMetricValidator(): TieredLockerMetricValidator {
    return TieredLockerMetricValidator(
        mutableListOf<TieredLockerMetricRule>(
            TieredLockerMetricRequiredDepotRule("tiered locker metric needs an entry"),
            TieredLockerMetricRequiredContactEmailRule("tiered locker metric needs a value"),
            TieredLockerMetricDepotLengthRule(1000),
        ),
    )
}
