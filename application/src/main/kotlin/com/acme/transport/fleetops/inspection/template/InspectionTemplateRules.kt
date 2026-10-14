package com.acme.transport.fleetops.inspection.template

/** A rule a inspection template must pass. Check returns its problems, or none. */
interface InspectionTemplateRule {
    fun check(item: InspectionTemplate): MutableList<String>
}

data class InspectionTemplateRequiredLabelRule(var message: String) : InspectionTemplateRule {
    override fun check(item: InspectionTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionTemplateDiscountRangeRule(var min: Double, var max: Double) : InspectionTemplateRule {
    override fun check(item: InspectionTemplate): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionTemplateEndsAtRangeRule(var min: Int, var max: Int) : InspectionTemplateRule {
    override fun check(item: InspectionTemplate): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a inspection template. */
data class InspectionTemplateValidator(var rules: MutableList<InspectionTemplateRule>) {
    fun validate(item: InspectionTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionTemplateValidator(): InspectionTemplateValidator {
    return InspectionTemplateValidator(
        mutableListOf<InspectionTemplateRule>(
            InspectionTemplateRequiredLabelRule("inspection template needs a value"),
            InspectionTemplateDiscountRangeRule(6.2, 444.4),
            InspectionTemplateEndsAtRangeRule(255, 255),
        ),
    )
}
