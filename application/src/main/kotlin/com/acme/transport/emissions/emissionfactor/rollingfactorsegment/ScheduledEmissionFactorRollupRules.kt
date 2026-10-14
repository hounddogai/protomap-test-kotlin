package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

/** A rule a scheduled emission factor rollup must pass. Check returns its problems, or none. */
interface ScheduledEmissionFactorRollupRule {
    fun check(item: ScheduledEmissionFactorRollup): MutableList<String>
}

data class ScheduledEmissionFactorRollupCodeLengthRule(var maxLength: Int) : ScheduledEmissionFactorRollupRule {
    override fun check(item: ScheduledEmissionFactorRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledEmissionFactorRollupMarginRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledEmissionFactorRollupRule {
    override fun check(item: ScheduledEmissionFactorRollup): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledEmissionFactorRollupRequiredCodeRule(
    var message: String,
) : ScheduledEmissionFactorRollupRule {
    override fun check(item: ScheduledEmissionFactorRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled emission factor rollup. */
data class ScheduledEmissionFactorRollupValidator(var rules: MutableList<ScheduledEmissionFactorRollupRule>) {
    fun validate(item: ScheduledEmissionFactorRollup): MutableList<String> {
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

    fun requireValid(item: ScheduledEmissionFactorRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledEmissionFactorRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledEmissionFactorRollupValidator(): ScheduledEmissionFactorRollupValidator {
    return ScheduledEmissionFactorRollupValidator(
        mutableListOf<ScheduledEmissionFactorRollupRule>(
            ScheduledEmissionFactorRollupCodeLengthRule(1000),
            ScheduledEmissionFactorRollupMarginRangeRule(8.3, 2265.1),
            ScheduledEmissionFactorRollupRequiredCodeRule("scheduled emission factor rollup needs this field"),
        ),
    )
}
