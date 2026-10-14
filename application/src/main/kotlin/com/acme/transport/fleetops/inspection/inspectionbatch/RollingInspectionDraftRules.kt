package com.acme.transport.fleetops.inspection.inspectionbatch

/** A rule a rolling inspection draft must pass. Check returns its problems, or none. */
interface RollingInspectionDraftRule {
    fun check(item: RollingInspectionDraft): MutableList<String>
}

data class RollingInspectionDraftStopCountRangeRule(var min: Int, var max: Int) : RollingInspectionDraftRule {
    override fun check(item: RollingInspectionDraft): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionDraftDescriptionLengthRule(var maxLength: Int) : RollingInspectionDraftRule {
    override fun check(item: RollingInspectionDraft): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionDraftCodePrefixRule(var prefix: String) : RollingInspectionDraftRule {
    override fun check(item: RollingInspectionDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionDraftRequiredDescriptionRule(var message: String) : RollingInspectionDraftRule {
    override fun check(item: RollingInspectionDraft): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingInspectionDraftTitleLengthRule(var maxLength: Int) : RollingInspectionDraftRule {
    override fun check(item: RollingInspectionDraft): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling inspection draft. */
data class RollingInspectionDraftValidator(var rules: MutableList<RollingInspectionDraftRule>) {
    fun validate(item: RollingInspectionDraft): MutableList<String> {
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

    fun requireValid(item: RollingInspectionDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingInspectionDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingInspectionDraftValidator(): RollingInspectionDraftValidator {
    return RollingInspectionDraftValidator(
        mutableListOf<RollingInspectionDraftRule>(
            RollingInspectionDraftStopCountRangeRule(32, 80),
            RollingInspectionDraftDescriptionLengthRule(255),
            RollingInspectionDraftCodePrefixRule("REF"),
            RollingInspectionDraftRequiredDescriptionRule("rolling inspection draft needs an entry"),
            RollingInspectionDraftTitleLengthRule(255),
        ),
    )
}
