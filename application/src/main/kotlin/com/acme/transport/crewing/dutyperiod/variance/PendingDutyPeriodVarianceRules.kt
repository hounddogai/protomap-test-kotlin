package com.acme.transport.crewing.dutyperiod.variance

/** A rule a pending duty period variance must pass. Check returns its problems, or none. */
interface PendingDutyPeriodVarianceRule {
    fun check(item: PendingDutyPeriodVariance): MutableList<String>
}

data class PendingDutyPeriodVarianceRequiredCodeRule(var message: String) : PendingDutyPeriodVarianceRule {
    override fun check(item: PendingDutyPeriodVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodVarianceVisibleGuardRule(var limit: Double) : PendingDutyPeriodVarianceRule {
    override fun check(item: PendingDutyPeriodVariance): MutableList<String> {
        if (item.visible && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class PendingDutyPeriodVarianceRatioRangeRule(
    var min: Double,
    var max: Double,
) : PendingDutyPeriodVarianceRule {
    override fun check(item: PendingDutyPeriodVariance): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending duty period variance. */
data class PendingDutyPeriodVarianceValidator(var rules: MutableList<PendingDutyPeriodVarianceRule>) {
    fun validate(item: PendingDutyPeriodVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingDutyPeriodVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDutyPeriodVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDutyPeriodVarianceValidator(): PendingDutyPeriodVarianceValidator {
    return PendingDutyPeriodVarianceValidator(
        mutableListOf<PendingDutyPeriodVarianceRule>(
            PendingDutyPeriodVarianceRequiredCodeRule("pending duty period variance needs a value"),
            PendingDutyPeriodVarianceVisibleGuardRule(5.6),
            PendingDutyPeriodVarianceRatioRangeRule(1.1, 2229.5),
        ),
    )
}
