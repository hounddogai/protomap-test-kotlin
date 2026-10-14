package com.acme.transport.customs.inspection.adjustment

/** A rule a fallback inspection adjustment must pass. Check returns its problems, or none. */
interface FallbackInspectionAdjustmentRule {
    fun check(item: FallbackInspectionAdjustment): MutableList<String>
}

data class FallbackInspectionAdjustmentRequiredSegmentRule(
    var message: String,
) : FallbackInspectionAdjustmentRule {
    override fun check(item: FallbackInspectionAdjustment): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionAdjustmentRequiredCodeRule(
    var message: String,
) : FallbackInspectionAdjustmentRule {
    override fun check(item: FallbackInspectionAdjustment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionAdjustmentSegmentLengthRule(
    var maxLength: Int,
) : FallbackInspectionAdjustmentRule {
    override fun check(item: FallbackInspectionAdjustment): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback inspection adjustment. */
data class FallbackInspectionAdjustmentValidator(var rules: MutableList<FallbackInspectionAdjustmentRule>) {
    fun validate(item: FallbackInspectionAdjustment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackInspectionAdjustment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackInspectionAdjustment>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackInspectionAdjustmentValidator(): FallbackInspectionAdjustmentValidator {
    return FallbackInspectionAdjustmentValidator(
        mutableListOf<FallbackInspectionAdjustmentRule>(
            FallbackInspectionAdjustmentRequiredSegmentRule("fallback inspection adjustment needs this field"),
            FallbackInspectionAdjustmentRequiredCodeRule("fallback inspection adjustment needs an entry"),
            FallbackInspectionAdjustmentSegmentLengthRule(32),
        ),
    )
}
