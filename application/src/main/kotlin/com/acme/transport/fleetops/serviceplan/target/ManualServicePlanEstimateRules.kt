package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.kit.text.normalize

/** A rule a manual service plan estimate must pass. Check returns its problems, or none. */
interface ManualServicePlanEstimateRule {
    fun check(item: ManualServicePlanEstimate): MutableList<String>
}

data class ManualServicePlanEstimateAllowedContactEmailRule(
    var allowed: MutableList<String>,
) : ManualServicePlanEstimateRule {
    override fun check(item: ManualServicePlanEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is too long")
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanEstimateCodeLengthRule(var maxLength: Int) : ManualServicePlanEstimateRule {
    override fun check(item: ManualServicePlanEstimate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanEstimateRequiredDescriptionRule(
    var message: String,
) : ManualServicePlanEstimateRule {
    override fun check(item: ManualServicePlanEstimate): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanEstimateTitleLengthRule(var maxLength: Int) : ManualServicePlanEstimateRule {
    override fun check(item: ManualServicePlanEstimate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanEstimateNotesLengthRule(var maxLength: Int) : ManualServicePlanEstimateRule {
    override fun check(item: ManualServicePlanEstimate): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual service plan estimate. */
data class ManualServicePlanEstimateValidator(var rules: MutableList<ManualServicePlanEstimateRule>) {
    fun validate(item: ManualServicePlanEstimate): MutableList<String> {
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

    fun requireValid(item: ManualServicePlanEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualServicePlanEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultManualServicePlanEstimateValidator(): ManualServicePlanEstimateValidator {
    return ManualServicePlanEstimateValidator(
        mutableListOf<ManualServicePlanEstimateRule>(
            ManualServicePlanEstimateAllowedContactEmailRule(mutableListOf<String>("weighted")),
            ManualServicePlanEstimateCodeLengthRule(3600),
            ManualServicePlanEstimateRequiredDescriptionRule("manual service plan estimate needs an entry"),
            ManualServicePlanEstimateTitleLengthRule(120),
            ManualServicePlanEstimateNotesLengthRule(120),
        ),
    )
}
