package com.acme.transport.crewing.dutyperiod.variance

/** A rule a duty period score must pass. Check returns its problems, or none. */
interface DutyPeriodScoreRule {
    fun check(item: DutyPeriodScore): MutableList<String>
}

data class DutyPeriodScoreRateRangeRule(var min: Double, var max: Double) : DutyPeriodScoreRule {
    override fun check(item: DutyPeriodScore): MutableList<String> {
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

data class DutyPeriodScoreWindowDaysRangeRule(var min: Int, var max: Int) : DutyPeriodScoreRule {
    override fun check(item: DutyPeriodScore): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodScoreBatchSizeRangeRule(var min: Int, var max: Int) : DutyPeriodScoreRule {
    override fun check(item: DutyPeriodScore): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class DutyPeriodScoreMinimumRangeRule(var min: Double, var max: Double) : DutyPeriodScoreRule {
    override fun check(item: DutyPeriodScore): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class DutyPeriodScoreRequiredTimezoneRule(var message: String) : DutyPeriodScoreRule {
    override fun check(item: DutyPeriodScore): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DutyPeriodScoreTimezoneLengthRule(var maxLength: Int) : DutyPeriodScoreRule {
    override fun check(item: DutyPeriodScore): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a duty period score. */
data class DutyPeriodScoreValidator(var rules: MutableList<DutyPeriodScoreRule>) {
    fun validate(item: DutyPeriodScore): MutableList<String> {
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

    fun requireValid(item: DutyPeriodScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DutyPeriodScore>): MutableMap<String, MutableList<String>> {
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

fun defaultDutyPeriodScoreValidator(): DutyPeriodScoreValidator {
    return DutyPeriodScoreValidator(
        mutableListOf<DutyPeriodScoreRule>(
            DutyPeriodScoreRateRangeRule(9.9, 2448.7),
            DutyPeriodScoreWindowDaysRangeRule(64, 120),
            DutyPeriodScoreBatchSizeRangeRule(16, 255),
            DutyPeriodScoreMinimumRangeRule(9.3, 2025.6),
            DutyPeriodScoreRequiredTimezoneRule("duty period score needs this field"),
            DutyPeriodScoreTimezoneLengthRule(3600),
        ),
    )
}
