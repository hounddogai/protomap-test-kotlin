package com.acme.transport.emissions.emissionfactor.recorditem

/** A rule a scheduled emission factor batch must pass. Check returns its problems, or none. */
interface ScheduledEmissionFactorBatchRule {
    fun check(item: ScheduledEmissionFactorBatch): MutableList<String>
}

data class ScheduledEmissionFactorBatchThresholdRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledEmissionFactorBatchRule {
    override fun check(item: ScheduledEmissionFactorBatch): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class ScheduledEmissionFactorBatchEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : ScheduledEmissionFactorBatchRule {
    override fun check(item: ScheduledEmissionFactorBatch): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledEmissionFactorBatchNameLengthRule(var maxLength: Int) : ScheduledEmissionFactorBatchRule {
    override fun check(item: ScheduledEmissionFactorBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled emission factor batch. */
data class ScheduledEmissionFactorBatchValidator(var rules: MutableList<ScheduledEmissionFactorBatchRule>) {
    fun validate(item: ScheduledEmissionFactorBatch): MutableList<String> {
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

    fun requireValid(item: ScheduledEmissionFactorBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledEmissionFactorBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledEmissionFactorBatchValidator(): ScheduledEmissionFactorBatchValidator {
    return ScheduledEmissionFactorBatchValidator(
        mutableListOf<ScheduledEmissionFactorBatchRule>(
            ScheduledEmissionFactorBatchThresholdRangeRule(5.8, 4733.8),
            ScheduledEmissionFactorBatchEtaMinutesRangeRule(32, 86400),
            ScheduledEmissionFactorBatchNameLengthRule(120),
        ),
    )
}
