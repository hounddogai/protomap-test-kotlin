package com.acme.transport.customs.inspection.draft

/** A rule a inspection draft must pass. Check returns its problems, or none. */
interface InspectionDraftRule {
    fun check(item: InspectionDraft): MutableList<String>
}

data class InspectionDraftCeilingRangeRule(var min: Double, var max: Double) : InspectionDraftRule {
    override fun check(item: InspectionDraft): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class InspectionDraftNameLengthRule(var maxLength: Int) : InspectionDraftRule {
    override fun check(item: InspectionDraft): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionDraftLaneLengthRule(var maxLength: Int) : InspectionDraftRule {
    override fun check(item: InspectionDraft): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class InspectionDraftCodePrefixRule(var prefix: String) : InspectionDraftRule {
    override fun check(item: InspectionDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class InspectionDraftRequiredCodeRule(var message: String) : InspectionDraftRule {
    override fun check(item: InspectionDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a inspection draft. */
data class InspectionDraftValidator(var rules: MutableList<InspectionDraftRule>) {
    fun validate(item: InspectionDraft): MutableList<String> {
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

    fun requireValid(item: InspectionDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<InspectionDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultInspectionDraftValidator(): InspectionDraftValidator {
    return InspectionDraftValidator(
        mutableListOf<InspectionDraftRule>(
            InspectionDraftCeilingRangeRule(2.2, 1661.8),
            InspectionDraftNameLengthRule(255),
            InspectionDraftLaneLengthRule(64),
            InspectionDraftCodePrefixRule("X"),
            InspectionDraftRequiredCodeRule("inspection draft needs a value"),
        ),
    )
}
