package com.acme.transport.customs.clearance.segment

/** A rule a clearance plan must pass. Check returns its problems, or none. */
interface ClearancePlanRule {
    fun check(item: ClearancePlan): MutableList<String>
}

data class ClearancePlanDistanceKmRangeRule(var min: Double, var max: Double) : ClearancePlanRule {
    override fun check(item: ClearancePlan): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ClearancePlanDepotLengthRule(var maxLength: Int) : ClearancePlanRule {
    override fun check(item: ClearancePlan): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearancePlanCodePrefixRule(var prefix: String) : ClearancePlanRule {
    override fun check(item: ClearancePlan): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ClearancePlanLabelLengthRule(var maxLength: Int) : ClearancePlanRule {
    override fun check(item: ClearancePlan): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearancePlanRequiredDepotRule(var message: String) : ClearancePlanRule {
    override fun check(item: ClearancePlan): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance plan. */
data class ClearancePlanValidator(var rules: MutableList<ClearancePlanRule>) {
    fun validate(item: ClearancePlan): MutableList<String> {
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

    fun requireValid(item: ClearancePlan) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearancePlan>): MutableMap<String, MutableList<String>> {
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

fun defaultClearancePlanValidator(): ClearancePlanValidator {
    return ClearancePlanValidator(
        mutableListOf<ClearancePlanRule>(
            ClearancePlanDistanceKmRangeRule(4.8, 2417.7),
            ClearancePlanDepotLengthRule(1000),
            ClearancePlanCodePrefixRule("Q"),
            ClearancePlanLabelLengthRule(1000),
            ClearancePlanRequiredDepotRule("clearance plan needs an entry"),
        ),
    )
}
