package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

/** A rule a manual accessorial adjustment must pass. Check returns its problems, or none. */
interface ManualAccessorialAdjustmentRule {
    fun check(item: ManualAccessorialAdjustment): MutableList<String>
}

data class ManualAccessorialAdjustmentCodePrefixRule(var prefix: String) : ManualAccessorialAdjustmentRule {
    override fun check(item: ManualAccessorialAdjustment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualAccessorialAdjustmentVersionRangeRule(
    var min: Int,
    var max: Int,
) : ManualAccessorialAdjustmentRule {
    override fun check(item: ManualAccessorialAdjustment): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class ManualAccessorialAdjustmentLimitRangeRule(
    var min: Int,
    var max: Int,
) : ManualAccessorialAdjustmentRule {
    override fun check(item: ManualAccessorialAdjustment): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class ManualAccessorialAdjustmentScoreRangeRule(
    var min: Double,
    var max: Double,
) : ManualAccessorialAdjustmentRule {
    override fun check(item: ManualAccessorialAdjustment): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual accessorial adjustment. */
data class ManualAccessorialAdjustmentValidator(var rules: MutableList<ManualAccessorialAdjustmentRule>) {
    fun validate(item: ManualAccessorialAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualAccessorialAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualAccessorialAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualAccessorialAdjustmentValidator(): ManualAccessorialAdjustmentValidator {
    return ManualAccessorialAdjustmentValidator(
        mutableListOf<ManualAccessorialAdjustmentRule>(
            ManualAccessorialAdjustmentCodePrefixRule("X"),
            ManualAccessorialAdjustmentVersionRangeRule(80, 120),
            ManualAccessorialAdjustmentLimitRangeRule(255, 3600),
            ManualAccessorialAdjustmentScoreRangeRule(8.5, 886.0),
        ),
    )
}
