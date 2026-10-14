package com.acme.transport.customs.inspection.adjustment

/** A rule a inspection baseline must pass. Check returns its problems, or none. */
interface InspectionBaselineRule {
    fun check(item: InspectionBaseline): MutableList<String>
}

data class InspectionBaselineRequiredLabelRule(var message: String) : InspectionBaselineRule {
    override fun check(item: InspectionBaseline): MutableList<String> {
        if (item.label.trim() == "") {
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

data class InspectionBaselineRouteCodeLengthRule(var maxLength: Int) : InspectionBaselineRule {
    override fun check(item: InspectionBaseline): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionBaselineApprovedGuardRule(var limit: Int) : InspectionBaselineRule {
    override fun check(item: InspectionBaseline): MutableList<String> {
        if (item.approved && item.quantity > limit) {
            return mutableListOf<String>("quantity ${item.quantity} exceeds $limit while approved")
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
            InspectionBaselineRequiredLabelRule("inspection baseline needs a value"),
            InspectionBaselineRequiredCodeRule("inspection baseline needs an entry"),
            InspectionBaselineRouteCodeLengthRule(64),
            InspectionBaselineApprovedGuardRule(80),
        ),
    )
}
