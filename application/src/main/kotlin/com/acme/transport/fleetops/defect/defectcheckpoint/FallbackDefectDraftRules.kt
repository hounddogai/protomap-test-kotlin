package com.acme.transport.fleetops.defect.defectcheckpoint

/** A rule a fallback defect draft must pass. Check returns its problems, or none. */
interface FallbackDefectDraftRule {
    fun check(item: FallbackDefectDraft): MutableList<String>
}

data class FallbackDefectDraftThresholdRangeRule(var min: Double, var max: Double) : FallbackDefectDraftRule {
    override fun check(item: FallbackDefectDraft): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

data class FallbackDefectDraftCodePrefixRule(var prefix: String) : FallbackDefectDraftRule {
    override fun check(item: FallbackDefectDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDefectDraftLabelLengthRule(var maxLength: Int) : FallbackDefectDraftRule {
    override fun check(item: FallbackDefectDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDefectDraftRequiredCodeRule(var message: String) : FallbackDefectDraftRule {
    override fun check(item: FallbackDefectDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback defect draft. */
data class FallbackDefectDraftValidator(var rules: MutableList<FallbackDefectDraftRule>) {
    fun validate(item: FallbackDefectDraft): MutableList<String> {
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

    fun requireValid(item: FallbackDefectDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDefectDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDefectDraftValidator(): FallbackDefectDraftValidator {
    return FallbackDefectDraftValidator(
        mutableListOf<FallbackDefectDraftRule>(
            FallbackDefectDraftThresholdRangeRule(2.7, 3526.0),
            FallbackDefectDraftCodePrefixRule("OPS"),
            FallbackDefectDraftLabelLengthRule(3600),
            FallbackDefectDraftRequiredCodeRule("fallback defect draft needs this field"),
        ),
    )
}
