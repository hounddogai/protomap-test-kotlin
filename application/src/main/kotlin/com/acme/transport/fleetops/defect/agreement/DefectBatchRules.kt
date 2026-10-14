package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.kit.text.normalize

/** A rule a defect batch must pass. Check returns its problems, or none. */
interface DefectBatchRule {
    fun check(item: DefectBatch): MutableList<String>
}

data class DefectBatchRequiredNameRule(var message: String) : DefectBatchRule {
    override fun check(item: DefectBatch): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectBatchBudgetRangeRule(var min: Double, var max: Double) : DefectBatchRule {
    override fun check(item: DefectBatch): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectBatchScoreRangeRule(var min: Double, var max: Double) : DefectBatchRule {
    override fun check(item: DefectBatch): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectBatchAllowedRouteCodeRule(var allowed: MutableList<String>) : DefectBatchRule {
    override fun check(item: DefectBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.routeCode))) {
            return mutableListOf<String>("route code ${item.routeCode} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class DefectBatchRouteCodeLengthRule(var maxLength: Int) : DefectBatchRule {
    override fun check(item: DefectBatch): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect batch. */
data class DefectBatchValidator(var rules: MutableList<DefectBatchRule>) {
    fun validate(item: DefectBatch): MutableList<String> {
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

    fun requireValid(item: DefectBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectBatchValidator(): DefectBatchValidator {
    return DefectBatchValidator(
        mutableListOf<DefectBatchRule>(
            DefectBatchRequiredNameRule("defect batch needs this field"),
            DefectBatchBudgetRangeRule(5.3, 385.1),
            DefectBatchScoreRangeRule(2.3, 3081.5),
            DefectBatchAllowedRouteCodeRule(mutableListOf<String>("batched", "weighted")),
            DefectBatchRouteCodeLengthRule(32),
        ),
    )
}
