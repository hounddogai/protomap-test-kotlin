package com.acme.transport.fleetops.inspection.inspectionpolicy

/** A rule a inspection baseline must pass. Check returns its problems, or none. */
interface InspectionBaselineRule {
    fun check(item: InspectionBaseline): MutableList<String>
}

data class InspectionBaselineCodeLengthRule(var maxLength: Int) : InspectionBaselineRule {
    override fun check(item: InspectionBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionBaselineRequiredTitleRule(var message: String) : InspectionBaselineRule {
    override fun check(item: InspectionBaseline): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionBaselineRequiredCodeRule(var message: String) : InspectionBaselineRule {
    override fun check(item: InspectionBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionBaselineRequiredLabelRule(var message: String) : InspectionBaselineRule {
    override fun check(item: InspectionBaseline): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection baseline. */
data class InspectionBaselineValidator(var rules: MutableList<InspectionBaselineRule>) {
    fun validate(item: InspectionBaseline): MutableList<String> {
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

    fun requireValid(item: InspectionBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionBaselineValidator(): InspectionBaselineValidator {
    return InspectionBaselineValidator(
        mutableListOf<InspectionBaselineRule>(
            InspectionBaselineCodeLengthRule(120),
            InspectionBaselineRequiredTitleRule("inspection baseline needs a value"),
            InspectionBaselineRequiredCodeRule("inspection baseline needs this field"),
            InspectionBaselineRequiredLabelRule("inspection baseline needs this field"),
        ),
    )
}
