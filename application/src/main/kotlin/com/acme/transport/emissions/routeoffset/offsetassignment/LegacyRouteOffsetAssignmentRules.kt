package com.acme.transport.emissions.routeoffset.offsetassignment

/** A rule a legacy route offset assignment must pass. Check returns its problems, or none. */
interface LegacyRouteOffsetAssignmentRule {
    fun check(item: LegacyRouteOffsetAssignment): MutableList<String>
}

data class LegacyRouteOffsetAssignmentCodeLengthRule(var maxLength: Int) : LegacyRouteOffsetAssignmentRule {
    override fun check(item: LegacyRouteOffsetAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRouteOffsetAssignmentRequiredCountryRule(
    var message: String,
) : LegacyRouteOffsetAssignmentRule {
    override fun check(item: LegacyRouteOffsetAssignment): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyRouteOffsetAssignmentOdometerRangeRule(
    var min: Int,
    var max: Int,
) : LegacyRouteOffsetAssignmentRule {
    override fun check(item: LegacyRouteOffsetAssignment): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class LegacyRouteOffsetAssignmentLabelLengthRule(var maxLength: Int) : LegacyRouteOffsetAssignmentRule {
    override fun check(item: LegacyRouteOffsetAssignment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyRouteOffsetAssignmentRequiredCodeRule(var message: String) : LegacyRouteOffsetAssignmentRule {
    override fun check(item: LegacyRouteOffsetAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy route offset assignment. */
data class LegacyRouteOffsetAssignmentValidator(var rules: MutableList<LegacyRouteOffsetAssignmentRule>) {
    fun validate(item: LegacyRouteOffsetAssignment): MutableList<String> {
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

    fun requireValid(item: LegacyRouteOffsetAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyRouteOffsetAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyRouteOffsetAssignmentValidator(): LegacyRouteOffsetAssignmentValidator {
    return LegacyRouteOffsetAssignmentValidator(
        mutableListOf<LegacyRouteOffsetAssignmentRule>(
            LegacyRouteOffsetAssignmentCodeLengthRule(120),
            LegacyRouteOffsetAssignmentRequiredCountryRule("legacy route offset assignment needs a value"),
            LegacyRouteOffsetAssignmentOdometerRangeRule(80, 1000),
            LegacyRouteOffsetAssignmentLabelLengthRule(120),
            LegacyRouteOffsetAssignmentRequiredCodeRule("legacy route offset assignment needs this field"),
        ),
    )
}
