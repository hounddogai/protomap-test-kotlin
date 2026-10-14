package com.acme.transport.fleetops.inspection.inspectionbatch

/** A rule a capped inspection draft must pass. Check returns its problems, or none. */
interface CappedInspectionDraftRule {
    fun check(item: CappedInspectionDraft): MutableList<String>
}

data class CappedInspectionDraftCodeLengthRule(var maxLength: Int) : CappedInspectionDraftRule {
    override fun check(item: CappedInspectionDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionDraftCodePrefixRule(var prefix: String) : CappedInspectionDraftRule {
    override fun check(item: CappedInspectionDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionDraftPalletsRangeRule(var min: Int, var max: Int) : CappedInspectionDraftRule {
    override fun check(item: CappedInspectionDraft): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedInspectionDraftCostRangeRule(var min: Double, var max: Double) : CappedInspectionDraftRule {
    override fun check(item: CappedInspectionDraft): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped inspection draft. */
data class CappedInspectionDraftValidator(var rules: MutableList<CappedInspectionDraftRule>) {
    fun validate(item: CappedInspectionDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedInspectionDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedInspectionDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedInspectionDraftValidator(): CappedInspectionDraftValidator {
    return CappedInspectionDraftValidator(
        mutableListOf<CappedInspectionDraftRule>(
            CappedInspectionDraftCodeLengthRule(1000),
            CappedInspectionDraftCodePrefixRule("ACM"),
            CappedInspectionDraftPalletsRangeRule(3600, 86400),
            CappedInspectionDraftCostRangeRule(1.5, 4330.1),
        ),
    )
}
