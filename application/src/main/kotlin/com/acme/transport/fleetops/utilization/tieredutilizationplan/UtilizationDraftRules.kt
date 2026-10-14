package com.acme.transport.fleetops.utilization.tieredutilizationplan

/** A rule a utilization draft must pass. Check returns its problems, or none. */
interface UtilizationDraftRule {
    fun check(item: UtilizationDraft): MutableList<String>
}

data class UtilizationDraftCodePrefixRule(var prefix: String) : UtilizationDraftRule {
    override fun check(item: UtilizationDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationDraftRouteCodeLengthRule(var maxLength: Int) : UtilizationDraftRule {
    override fun check(item: UtilizationDraft): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class UtilizationDraftRequiredCodeRule(var message: String) : UtilizationDraftRule {
    override fun check(item: UtilizationDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a utilization draft. */
data class UtilizationDraftValidator(var rules: MutableList<UtilizationDraftRule>) {
    fun validate(item: UtilizationDraft): MutableList<String> {
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

    fun requireValid(item: UtilizationDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<UtilizationDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultUtilizationDraftValidator(): UtilizationDraftValidator {
    return UtilizationDraftValidator(
        mutableListOf<UtilizationDraftRule>(
            UtilizationDraftCodePrefixRule("Q"),
            UtilizationDraftRouteCodeLengthRule(120),
            UtilizationDraftRequiredCodeRule("utilization draft needs a value"),
        ),
    )
}
