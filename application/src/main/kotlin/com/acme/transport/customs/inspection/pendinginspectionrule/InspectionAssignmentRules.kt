package com.acme.transport.customs.inspection.pendinginspectionrule

/** A rule a inspection assignment must pass. Check returns its problems, or none. */
interface InspectionAssignmentRule {
    fun check(item: InspectionAssignment): MutableList<String>
}

data class InspectionAssignmentRequiredLabelRule(var message: String) : InspectionAssignmentRule {
    override fun check(item: InspectionAssignment): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionAssignmentRatioRangeRule(var min: Double, var max: Double) : InspectionAssignmentRule {
    override fun check(item: InspectionAssignment): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class InspectionAssignmentCodeLengthRule(var maxLength: Int) : InspectionAssignmentRule {
    override fun check(item: InspectionAssignment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionAssignmentEtaMinutesRangeRule(var min: Int, var max: Int) : InspectionAssignmentRule {
    override fun check(item: InspectionAssignment): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class InspectionAssignmentScoreRangeRule(var min: Double, var max: Double) : InspectionAssignmentRule {
    override fun check(item: InspectionAssignment): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a inspection assignment. */
data class InspectionAssignmentValidator(var rules: MutableList<InspectionAssignmentRule>) {
    fun validate(item: InspectionAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionAssignmentValidator(): InspectionAssignmentValidator {
    return InspectionAssignmentValidator(
        mutableListOf<InspectionAssignmentRule>(
            InspectionAssignmentRequiredLabelRule("inspection assignment needs a value"),
            InspectionAssignmentRatioRangeRule(8.0, 1996.1),
            InspectionAssignmentCodeLengthRule(16),
            InspectionAssignmentEtaMinutesRangeRule(255, 86400),
            InspectionAssignmentScoreRangeRule(5.2, 634.4),
        ),
    )
}
