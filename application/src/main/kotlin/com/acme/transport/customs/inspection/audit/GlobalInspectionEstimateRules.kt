package com.acme.transport.customs.inspection.audit

/** A rule a global inspection estimate must pass. Check returns its problems, or none. */
interface GlobalInspectionEstimateRule {
    fun check(item: GlobalInspectionEstimate): MutableList<String>
}

data class GlobalInspectionEstimateDockLengthRule(var maxLength: Int) : GlobalInspectionEstimateRule {
    override fun check(item: GlobalInspectionEstimate): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionEstimateRequiredDockRule(var message: String) : GlobalInspectionEstimateRule {
    override fun check(item: GlobalInspectionEstimate): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionEstimateTitleLengthRule(var maxLength: Int) : GlobalInspectionEstimateRule {
    override fun check(item: GlobalInspectionEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global inspection estimate. */
data class GlobalInspectionEstimateValidator(var rules: MutableList<GlobalInspectionEstimateRule>) {
    fun validate(item: GlobalInspectionEstimate): MutableList<String> {
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

    fun requireValid(item: GlobalInspectionEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalInspectionEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalInspectionEstimateValidator(): GlobalInspectionEstimateValidator {
    return GlobalInspectionEstimateValidator(
        mutableListOf<GlobalInspectionEstimateRule>(
            GlobalInspectionEstimateDockLengthRule(64),
            GlobalInspectionEstimateRequiredDockRule("global inspection estimate needs an entry"),
            GlobalInspectionEstimateTitleLengthRule(120),
        ),
    )
}
