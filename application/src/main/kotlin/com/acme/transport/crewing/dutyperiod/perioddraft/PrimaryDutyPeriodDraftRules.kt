package com.acme.transport.crewing.dutyperiod.perioddraft

/** A rule a primary duty period draft must pass. Check returns its problems, or none. */
interface PrimaryDutyPeriodDraftRule {
    fun check(item: PrimaryDutyPeriodDraft): MutableList<String>
}

data class PrimaryDutyPeriodDraftLabelLengthRule(var maxLength: Int) : PrimaryDutyPeriodDraftRule {
    override fun check(item: PrimaryDutyPeriodDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDutyPeriodDraftFloorValueRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryDutyPeriodDraftRule {
    override fun check(item: PrimaryDutyPeriodDraft): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class PrimaryDutyPeriodDraftCategoryLengthRule(var maxLength: Int) : PrimaryDutyPeriodDraftRule {
    override fun check(item: PrimaryDutyPeriodDraft): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDutyPeriodDraftRequiredLocaleRule(var message: String) : PrimaryDutyPeriodDraftRule {
    override fun check(item: PrimaryDutyPeriodDraft): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDutyPeriodDraftCodeLengthRule(var maxLength: Int) : PrimaryDutyPeriodDraftRule {
    override fun check(item: PrimaryDutyPeriodDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary duty period draft. */
data class PrimaryDutyPeriodDraftValidator(var rules: MutableList<PrimaryDutyPeriodDraftRule>) {
    fun validate(item: PrimaryDutyPeriodDraft): MutableList<String> {
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

    fun requireValid(item: PrimaryDutyPeriodDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDutyPeriodDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDutyPeriodDraftValidator(): PrimaryDutyPeriodDraftValidator {
    return PrimaryDutyPeriodDraftValidator(
        mutableListOf<PrimaryDutyPeriodDraftRule>(
            PrimaryDutyPeriodDraftLabelLengthRule(1000),
            PrimaryDutyPeriodDraftFloorValueRangeRule(8.0, 2290.8),
            PrimaryDutyPeriodDraftCategoryLengthRule(255),
            PrimaryDutyPeriodDraftRequiredLocaleRule("primary duty period draft needs an entry"),
            PrimaryDutyPeriodDraftCodeLengthRule(64),
        ),
    )
}
