package com.acme.transport.customs.inspection.window

/** A rule a adaptive inspection window must pass. Check returns its problems, or none. */
interface AdaptiveInspectionWindowRule {
    fun check(item: AdaptiveInspectionWindow): MutableList<String>
}

data class AdaptiveInspectionWindowOwnerIdLengthRule(var maxLength: Int) : AdaptiveInspectionWindowRule {
    override fun check(item: AdaptiveInspectionWindow): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionWindowRequiredLabelRule(var message: String) : AdaptiveInspectionWindowRule {
    override fun check(item: AdaptiveInspectionWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionWindowCodePrefixRule(var prefix: String) : AdaptiveInspectionWindowRule {
    override fun check(item: AdaptiveInspectionWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionWindowEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : AdaptiveInspectionWindowRule {
    override fun check(item: AdaptiveInspectionWindow): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class AdaptiveInspectionWindowCodeLengthRule(var maxLength: Int) : AdaptiveInspectionWindowRule {
    override fun check(item: AdaptiveInspectionWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveInspectionWindowRequiredCodeRule(var message: String) : AdaptiveInspectionWindowRule {
    override fun check(item: AdaptiveInspectionWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive inspection window. */
data class AdaptiveInspectionWindowValidator(var rules: MutableList<AdaptiveInspectionWindowRule>) {
    fun validate(item: AdaptiveInspectionWindow): MutableList<String> {
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

    fun requireValid(item: AdaptiveInspectionWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveInspectionWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveInspectionWindowValidator(): AdaptiveInspectionWindowValidator {
    return AdaptiveInspectionWindowValidator(
        mutableListOf<AdaptiveInspectionWindowRule>(
            AdaptiveInspectionWindowOwnerIdLengthRule(32),
            AdaptiveInspectionWindowRequiredLabelRule("adaptive inspection window needs this field"),
            AdaptiveInspectionWindowCodePrefixRule("ACM"),
            AdaptiveInspectionWindowEtaMinutesRangeRule(16, 120),
            AdaptiveInspectionWindowCodeLengthRule(32),
            AdaptiveInspectionWindowRequiredCodeRule("adaptive inspection window needs an entry"),
        ),
    )
}
