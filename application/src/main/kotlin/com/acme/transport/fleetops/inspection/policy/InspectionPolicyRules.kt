package com.acme.transport.fleetops.inspection.policy

/** A rule a inspection policy must pass. Check returns its problems, or none. */
interface InspectionPolicyRule {
    fun check(item: InspectionPolicy): MutableList<String>
}

data class InspectionPolicyDistanceKmRangeRule(var min: Double, var max: Double) : InspectionPolicyRule {
    override fun check(item: InspectionPolicy): MutableList<String> {
        val value = item.distanceKm
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("distance km $value is below $min")
        }
        if (value > max) {
            problems.add("distance km $value is above $max")
        }
        return problems
    }
}

data class InspectionPolicyLabelLengthRule(var maxLength: Int) : InspectionPolicyRule {
    override fun check(item: InspectionPolicy): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionPolicyRateRangeRule(var min: Double, var max: Double) : InspectionPolicyRule {
    override fun check(item: InspectionPolicy): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionPolicyContactEmailLengthRule(var maxLength: Int) : InspectionPolicyRule {
    override fun check(item: InspectionPolicy): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection policy. */
data class InspectionPolicyValidator(var rules: MutableList<InspectionPolicyRule>) {
    fun validate(item: InspectionPolicy): MutableList<String> {
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

    fun requireValid(item: InspectionPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionPolicyValidator(): InspectionPolicyValidator {
    return InspectionPolicyValidator(
        mutableListOf<InspectionPolicyRule>(
            InspectionPolicyDistanceKmRangeRule(5.6, 1446.9),
            InspectionPolicyLabelLengthRule(16),
            InspectionPolicyRateRangeRule(3.6, 3249.0),
            InspectionPolicyContactEmailLengthRule(86400),
        ),
    )
}
