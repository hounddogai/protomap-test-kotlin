package com.acme.transport.customs.inspection.entry

/** A rule a adaptive inspection profile must pass. Check returns its problems, or none. */
interface AdaptiveInspectionProfileRule {
    fun check(item: AdaptiveInspectionProfile): MutableList<String>
}

data class AdaptiveInspectionProfileCodeLengthRule(var maxLength: Int) : AdaptiveInspectionProfileRule {
    override fun check(item: AdaptiveInspectionProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionProfileNameLengthRule(var maxLength: Int) : AdaptiveInspectionProfileRule {
    override fun check(item: AdaptiveInspectionProfile): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionProfileRequiredCodeRule(var message: String) : AdaptiveInspectionProfileRule {
    override fun check(item: AdaptiveInspectionProfile): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection profile. */
data class AdaptiveInspectionProfileValidator(var rules: MutableList<AdaptiveInspectionProfileRule>) {
    fun validate(item: AdaptiveInspectionProfile): MutableList<String> {
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

    fun requireValid(item: AdaptiveInspectionProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionProfileValidator(): AdaptiveInspectionProfileValidator {
    return AdaptiveInspectionProfileValidator(
        mutableListOf<AdaptiveInspectionProfileRule>(
            AdaptiveInspectionProfileCodeLengthRule(16),
            AdaptiveInspectionProfileNameLengthRule(86400),
            AdaptiveInspectionProfileRequiredCodeRule("adaptive inspection profile needs this field"),
        ),
    )
}
