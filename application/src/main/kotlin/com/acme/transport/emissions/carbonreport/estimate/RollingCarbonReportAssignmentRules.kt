package com.acme.transport.emissions.carbonreport.estimate

/** A rule a rolling carbon report assignment must pass. Check returns its problems, or none. */
interface RollingCarbonReportAssignmentRule {
    fun check(item: RollingCarbonReportAssignment): MutableList<String>
}

data class RollingCarbonReportAssignmentRequiredNameRule(
    var message: String,
) : RollingCarbonReportAssignmentRule {
    override fun check(item: RollingCarbonReportAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingCarbonReportAssignmentThresholdRangeRule(
    var min: Double,
    var max: Double,
) : RollingCarbonReportAssignmentRule {
    override fun check(item: RollingCarbonReportAssignment): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingCarbonReportAssignmentCodePrefixRule(var prefix: String) : RollingCarbonReportAssignmentRule {
    override fun check(item: RollingCarbonReportAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingCarbonReportAssignmentDepotLengthRule(
    var maxLength: Int,
) : RollingCarbonReportAssignmentRule {
    override fun check(item: RollingCarbonReportAssignment): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling carbon report assignment. */
data class RollingCarbonReportAssignmentValidator(var rules: MutableList<RollingCarbonReportAssignmentRule>) {
    fun validate(item: RollingCarbonReportAssignment): MutableList<String> {
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

    fun requireValid(item: RollingCarbonReportAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingCarbonReportAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingCarbonReportAssignmentValidator(): RollingCarbonReportAssignmentValidator {
    return RollingCarbonReportAssignmentValidator(
        mutableListOf<RollingCarbonReportAssignmentRule>(
            RollingCarbonReportAssignmentRequiredNameRule("rolling carbon report assignment needs an entry"),
            RollingCarbonReportAssignmentThresholdRangeRule(1.8, 719.0),
            RollingCarbonReportAssignmentCodePrefixRule("REF"),
            RollingCarbonReportAssignmentDepotLengthRule(64),
        ),
    )
}
