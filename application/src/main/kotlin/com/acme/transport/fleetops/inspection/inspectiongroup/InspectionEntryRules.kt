package com.acme.transport.fleetops.inspection.inspectiongroup

/** A rule a inspection entry must pass. Check returns its problems, or none. */
interface InspectionEntryRule {
    fun check(item: InspectionEntry): MutableList<String>
}

data class InspectionEntryLoadKgRangeRule(var min: Double, var max: Double) : InspectionEntryRule {
    override fun check(item: InspectionEntry): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class InspectionEntryRequiredCodeRule(var message: String) : InspectionEntryRule {
    override fun check(item: InspectionEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class InspectionEntryAmountRangeRule(var min: Double, var max: Double) : InspectionEntryRule {
    override fun check(item: InspectionEntry): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class InspectionEntryRequiredLabelRule(var message: String) : InspectionEntryRule {
    override fun check(item: InspectionEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection entry. */
data class InspectionEntryValidator(var rules: MutableList<InspectionEntryRule>) {
    fun validate(item: InspectionEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: InspectionEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionEntryValidator(): InspectionEntryValidator {
    return InspectionEntryValidator(
        mutableListOf<InspectionEntryRule>(
            InspectionEntryLoadKgRangeRule(2.0, 2980.5),
            InspectionEntryRequiredCodeRule("inspection entry needs this field"),
            InspectionEntryAmountRangeRule(4.9, 477.3),
            InspectionEntryRequiredLabelRule("inspection entry needs a value"),
        ),
    )
}
