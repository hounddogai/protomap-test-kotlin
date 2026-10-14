package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.text.normalize

/** A rule a capped sortation milestone must pass. Check returns its problems, or none. */
interface CappedSortationMilestoneRule {
    fun check(item: CappedSortationMilestone): MutableList<String>
}

data class CappedSortationMilestoneRequiredLabelRule(var message: String) : CappedSortationMilestoneRule {
    override fun check(item: CappedSortationMilestone): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedSortationMilestoneOwnerIdLengthRule(var maxLength: Int) : CappedSortationMilestoneRule {
    override fun check(item: CappedSortationMilestone): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedSortationMilestoneAllowedCodeRule(
    var allowed: MutableList<String>,
) : CappedSortationMilestoneRule {
    override fun check(item: CappedSortationMilestone): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is missing")
        }
        return mutableListOf<String>()
    }
}

data class CappedSortationMilestoneCodeLengthRule(var maxLength: Int) : CappedSortationMilestoneRule {
    override fun check(item: CappedSortationMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedSortationMilestoneRequiredOwnerIdRule(var message: String) : CappedSortationMilestoneRule {
    override fun check(item: CappedSortationMilestone): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedSortationMilestoneCodePrefixRule(var prefix: String) : CappedSortationMilestoneRule {
    override fun check(item: CappedSortationMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped sortation milestone. */
data class CappedSortationMilestoneValidator(var rules: MutableList<CappedSortationMilestoneRule>) {
    fun validate(item: CappedSortationMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedSortationMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedSortationMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedSortationMilestoneValidator(): CappedSortationMilestoneValidator {
    return CappedSortationMilestoneValidator(
        mutableListOf<CappedSortationMilestoneRule>(
            CappedSortationMilestoneRequiredLabelRule("capped sortation milestone needs a value"),
            CappedSortationMilestoneOwnerIdLengthRule(1000),
            CappedSortationMilestoneAllowedCodeRule(mutableListOf<String>("default")),
            CappedSortationMilestoneCodeLengthRule(64),
            CappedSortationMilestoneRequiredOwnerIdRule("capped sortation milestone needs this field"),
            CappedSortationMilestoneCodePrefixRule("X"),
        ),
    )
}
