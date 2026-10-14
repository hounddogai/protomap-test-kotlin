package com.acme.transport.crewing.qualification.request

/** A rule a regional qualification draft must pass. Check returns its problems, or none. */
interface RegionalQualificationDraftRule {
    fun check(item: RegionalQualificationDraft): MutableList<String>
}

data class RegionalQualificationDraftLabelLengthRule(var maxLength: Int) : RegionalQualificationDraftRule {
    override fun check(item: RegionalQualificationDraft): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationDraftStopCountRangeRule(
    var min: Int,
    var max: Int,
) : RegionalQualificationDraftRule {
    override fun check(item: RegionalQualificationDraft): MutableList<String> {
        val value = item.stopCount
        if (value < min || value > max) {
            return mutableListOf<String>("stop count $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationDraftRequiredNameRule(var message: String) : RegionalQualificationDraftRule {
    override fun check(item: RegionalQualificationDraft): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalQualificationDraftLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : RegionalQualificationDraftRule {
    override fun check(item: RegionalQualificationDraft): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class RegionalQualificationDraftActiveGuardRule(var limit: Int) : RegionalQualificationDraftRule {
    override fun check(item: RegionalQualificationDraft): MutableList<String> {
        if (item.active && item.stopCount > limit) {
            return mutableListOf<String>("stop count ${item.stopCount} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional qualification draft. */
data class RegionalQualificationDraftValidator(var rules: MutableList<RegionalQualificationDraftRule>) {
    fun validate(item: RegionalQualificationDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalQualificationDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalQualificationDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalQualificationDraftValidator(): RegionalQualificationDraftValidator {
    return RegionalQualificationDraftValidator(
        mutableListOf<RegionalQualificationDraftRule>(
            RegionalQualificationDraftLabelLengthRule(32),
            RegionalQualificationDraftStopCountRangeRule(255, 86400),
            RegionalQualificationDraftRequiredNameRule("regional qualification draft needs a value"),
            RegionalQualificationDraftLoadKgRangeRule(8.9, 3707.6),
            RegionalQualificationDraftActiveGuardRule(86400),
        ),
    )
}
