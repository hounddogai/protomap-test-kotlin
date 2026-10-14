package com.acme.transport.customs.inspection.weightedinspectionplan

/** A rule a global inspection group must pass. Check returns its problems, or none. */
interface GlobalInspectionGroupRule {
    fun check(item: GlobalInspectionGroup): MutableList<String>
}

data class GlobalInspectionGroupCodePrefixRule(var prefix: String) : GlobalInspectionGroupRule {
    override fun check(item: GlobalInspectionGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionGroupLocaleLengthRule(var maxLength: Int) : GlobalInspectionGroupRule {
    override fun check(item: GlobalInspectionGroup): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionGroupLabelLengthRule(var maxLength: Int) : GlobalInspectionGroupRule {
    override fun check(item: GlobalInspectionGroup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global inspection group. */
data class GlobalInspectionGroupValidator(var rules: MutableList<GlobalInspectionGroupRule>) {
    fun validate(item: GlobalInspectionGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: GlobalInspectionGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalInspectionGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalInspectionGroupValidator(): GlobalInspectionGroupValidator {
    return GlobalInspectionGroupValidator(
        mutableListOf<GlobalInspectionGroupRule>(
            GlobalInspectionGroupCodePrefixRule("Q"),
            GlobalInspectionGroupLocaleLengthRule(64),
            GlobalInspectionGroupLabelLengthRule(64),
        ),
    )
}
