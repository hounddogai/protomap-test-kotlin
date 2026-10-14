package com.acme.transport.crewing.roster.review

/** A rule a weighted roster draft must pass. Check returns its problems, or none. */
interface WeightedRosterDraftRule {
    fun check(item: WeightedRosterDraft): MutableList<String>
}

data class WeightedRosterDraftCodeLengthRule(var maxLength: Int) : WeightedRosterDraftRule {
    override fun check(item: WeightedRosterDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRosterDraftFloorValueRangeRule(var min: Double, var max: Double) : WeightedRosterDraftRule {
    override fun check(item: WeightedRosterDraft): MutableList<String> {
        val value = item.floorValue
        if (value < min || value > max) {
            return mutableListOf<String>("floor value $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRosterDraftCodePrefixRule(var prefix: String) : WeightedRosterDraftRule {
    override fun check(item: WeightedRosterDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedRosterDraftCategoryLengthRule(var maxLength: Int) : WeightedRosterDraftRule {
    override fun check(item: WeightedRosterDraft): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted roster draft. */
data class WeightedRosterDraftValidator(var rules: MutableList<WeightedRosterDraftRule>) {
    fun validate(item: WeightedRosterDraft): MutableList<String> {
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

    fun requireValid(item: WeightedRosterDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedRosterDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedRosterDraftValidator(): WeightedRosterDraftValidator {
    return WeightedRosterDraftValidator(
        mutableListOf<WeightedRosterDraftRule>(
            WeightedRosterDraftCodeLengthRule(16),
            WeightedRosterDraftFloorValueRangeRule(3.9, 3085.3),
            WeightedRosterDraftCodePrefixRule("X"),
            WeightedRosterDraftCategoryLengthRule(120),
        ),
    )
}
