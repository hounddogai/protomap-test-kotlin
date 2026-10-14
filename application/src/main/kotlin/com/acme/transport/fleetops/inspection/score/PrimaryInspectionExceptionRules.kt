package com.acme.transport.fleetops.inspection.score

import com.acme.transport.kit.text.normalize

/** A rule a primary inspection exception must pass. Check returns its problems, or none. */
interface PrimaryInspectionExceptionRule {
    fun check(item: PrimaryInspectionException): MutableList<String>
}

data class PrimaryInspectionExceptionLocaleLengthRule(var maxLength: Int) : PrimaryInspectionExceptionRule {
    override fun check(item: PrimaryInspectionException): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionExceptionTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : PrimaryInspectionExceptionRule {
    override fun check(item: PrimaryInspectionException): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class PrimaryInspectionExceptionAllowedLocaleRule(
    var allowed: MutableList<String>,
) : PrimaryInspectionExceptionRule {
    override fun check(item: PrimaryInspectionException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionExceptionRequiredLabelRule(var message: String) : PrimaryInspectionExceptionRule {
    override fun check(item: PrimaryInspectionException): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionExceptionLabelLengthRule(var maxLength: Int) : PrimaryInspectionExceptionRule {
    override fun check(item: PrimaryInspectionException): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryInspectionExceptionCodePrefixRule(var prefix: String) : PrimaryInspectionExceptionRule {
    override fun check(item: PrimaryInspectionException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary inspection exception. */
data class PrimaryInspectionExceptionValidator(var rules: MutableList<PrimaryInspectionExceptionRule>) {
    fun validate(item: PrimaryInspectionException): MutableList<String> {
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

    fun requireValid(item: PrimaryInspectionException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryInspectionException>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryInspectionExceptionValidator(): PrimaryInspectionExceptionValidator {
    return PrimaryInspectionExceptionValidator(
        mutableListOf<PrimaryInspectionExceptionRule>(
            PrimaryInspectionExceptionLocaleLengthRule(64),
            PrimaryInspectionExceptionTagsLimitRule(16, mutableListOf<String>("scheduled")),
            PrimaryInspectionExceptionAllowedLocaleRule(mutableListOf<String>("scheduled", "pending")),
            PrimaryInspectionExceptionRequiredLabelRule("primary inspection exception needs this field"),
            PrimaryInspectionExceptionLabelLengthRule(80),
            PrimaryInspectionExceptionCodePrefixRule("ACM"),
        ),
    )
}
