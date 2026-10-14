package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.text.normalize

/** A rule a fallback inspection notice must pass. Check returns its problems, or none. */
interface FallbackInspectionNoticeRule {
    fun check(item: FallbackInspectionNotice): MutableList<String>
}

data class FallbackInspectionNoticeRequiredNameRule(var message: String) : FallbackInspectionNoticeRule {
    override fun check(item: FallbackInspectionNotice): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionNoticeLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : FallbackInspectionNoticeRule {
    override fun check(item: FallbackInspectionNotice): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionNoticeAllowedNotesRule(
    var allowed: MutableList<String>,
) : FallbackInspectionNoticeRule {
    override fun check(item: FallbackInspectionNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is too long")
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionNoticeNameLengthRule(var maxLength: Int) : FallbackInspectionNoticeRule {
    override fun check(item: FallbackInspectionNotice): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionNoticeCategoryLengthRule(var maxLength: Int) : FallbackInspectionNoticeRule {
    override fun check(item: FallbackInspectionNotice): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback inspection notice. */
data class FallbackInspectionNoticeValidator(var rules: MutableList<FallbackInspectionNoticeRule>) {
    fun validate(item: FallbackInspectionNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackInspectionNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackInspectionNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackInspectionNoticeValidator(): FallbackInspectionNoticeValidator {
    return FallbackInspectionNoticeValidator(
        mutableListOf<FallbackInspectionNoticeRule>(
            FallbackInspectionNoticeRequiredNameRule("fallback inspection notice needs this field"),
            FallbackInspectionNoticeLoadKgRangeRule(7.3, 1099.2),
            FallbackInspectionNoticeAllowedNotesRule(mutableListOf<String>("batched")),
            FallbackInspectionNoticeNameLengthRule(3600),
            FallbackInspectionNoticeCategoryLengthRule(64),
        ),
    )
}
