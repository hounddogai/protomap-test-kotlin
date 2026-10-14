package com.acme.transport.emissions.emissionfactor.mapping

/** A rule a pending emission factor target must pass. Check returns its problems, or none. */
interface PendingEmissionFactorTargetRule {
    fun check(item: PendingEmissionFactorTarget): MutableList<String>
}

data class PendingEmissionFactorTargetThresholdRangeRule(
    var min: Double,
    var max: Double,
) : PendingEmissionFactorTargetRule {
    override fun check(item: PendingEmissionFactorTarget): MutableList<String> {
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

data class PendingEmissionFactorTargetRequiredPhoneRule(var message: String) : PendingEmissionFactorTargetRule {
    override fun check(item: PendingEmissionFactorTarget): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingEmissionFactorTargetRequiredCodeRule(var message: String) : PendingEmissionFactorTargetRule {
    override fun check(item: PendingEmissionFactorTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending emission factor target. */
data class PendingEmissionFactorTargetValidator(var rules: MutableList<PendingEmissionFactorTargetRule>) {
    fun validate(item: PendingEmissionFactorTarget): MutableList<String> {
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

    fun requireValid(item: PendingEmissionFactorTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingEmissionFactorTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingEmissionFactorTargetValidator(): PendingEmissionFactorTargetValidator {
    return PendingEmissionFactorTargetValidator(
        mutableListOf<PendingEmissionFactorTargetRule>(
            PendingEmissionFactorTargetThresholdRangeRule(4.3, 1879.6),
            PendingEmissionFactorTargetRequiredPhoneRule("pending emission factor target needs a value"),
            PendingEmissionFactorTargetRequiredCodeRule("pending emission factor target needs a value"),
        ),
    )
}
