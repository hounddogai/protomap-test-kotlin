package com.acme.transport.fleetops.inspection.score

/** A rule a inspection score must pass. Check returns its problems, or none. */
interface InspectionScoreRule {
    fun check(item: InspectionScore): MutableList<String>
}

data class InspectionScoreWeightRangeRule(var min: Double, var max: Double) : InspectionScoreRule {
    override fun check(item: InspectionScore): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionScoreLabelLengthRule(var maxLength: Int) : InspectionScoreRule {
    override fun check(item: InspectionScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionScoreBudgetRangeRule(var min: Double, var max: Double) : InspectionScoreRule {
    override fun check(item: InspectionScore): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a inspection score. */
data class InspectionScoreValidator(var rules: MutableList<InspectionScoreRule>) {
    fun validate(item: InspectionScore): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionScore>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionScoreValidator(): InspectionScoreValidator {
    return InspectionScoreValidator(
        mutableListOf<InspectionScoreRule>(
            InspectionScoreWeightRangeRule(4.0, 2444.1),
            InspectionScoreLabelLengthRule(3600),
            InspectionScoreBudgetRangeRule(0.1, 4609.9),
        ),
    )
}
