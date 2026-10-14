package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.text.normalize

/** A rule a pending declaration estimate must pass. Check returns its problems, or none. */
interface PendingDeclarationEstimateRule {
    fun check(item: PendingDeclarationEstimate): MutableList<String>
}

data class PendingDeclarationEstimateOdometerRangeRule(
    var min: Int,
    var max: Int,
) : PendingDeclarationEstimateRule {
    override fun check(item: PendingDeclarationEstimate): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class PendingDeclarationEstimateRequiredCodeRule(var message: String) : PendingDeclarationEstimateRule {
    override fun check(item: PendingDeclarationEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingDeclarationEstimateAllowedLabelRule(
    var allowed: MutableList<String>,
) : PendingDeclarationEstimateRule {
    override fun check(item: PendingDeclarationEstimate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending declaration estimate. */
data class PendingDeclarationEstimateValidator(var rules: MutableList<PendingDeclarationEstimateRule>) {
    fun validate(item: PendingDeclarationEstimate): MutableList<String> {
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

    fun requireValid(item: PendingDeclarationEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingDeclarationEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingDeclarationEstimateValidator(): PendingDeclarationEstimateValidator {
    return PendingDeclarationEstimateValidator(
        mutableListOf<PendingDeclarationEstimateRule>(
            PendingDeclarationEstimateOdometerRangeRule(120, 3600),
            PendingDeclarationEstimateRequiredCodeRule("pending declaration estimate needs this field"),
            PendingDeclarationEstimateAllowedLabelRule(mutableListOf<String>("global", "capped")),
        ),
    )
}
