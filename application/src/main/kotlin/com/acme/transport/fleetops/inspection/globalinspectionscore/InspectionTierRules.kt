package com.acme.transport.fleetops.inspection.globalinspectionscore

/** A rule a inspection tier must pass. Check returns its problems, or none. */
interface InspectionTierRule {
    fun check(item: InspectionTier): MutableList<String>
}

data class InspectionTierCodePrefixRule(var prefix: String) : InspectionTierRule {
    override fun check(item: InspectionTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionTierRequiredNameRule(var message: String) : InspectionTierRule {
    override fun check(item: InspectionTier): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionTierThresholdRangeRule(var min: Double, var max: Double) : InspectionTierRule {
    override fun check(item: InspectionTier): MutableList<String> {
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

/** Runs every rule of a inspection tier. */
data class InspectionTierValidator(var rules: MutableList<InspectionTierRule>) {
    fun validate(item: InspectionTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionTier>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionTierValidator(): InspectionTierValidator {
    return InspectionTierValidator(
        mutableListOf<InspectionTierRule>(
            InspectionTierCodePrefixRule("X"),
            InspectionTierRequiredNameRule("inspection tier needs this field"),
            InspectionTierThresholdRangeRule(8.7, 3145.3),
        ),
    )
}
