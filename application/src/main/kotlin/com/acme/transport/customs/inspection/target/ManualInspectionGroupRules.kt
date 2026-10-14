package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.text.normalize

/** A rule a manual inspection group must pass. Check returns its problems, or none. */
interface ManualInspectionGroupRule {
    fun check(item: ManualInspectionGroup): MutableList<String>
}

data class ManualInspectionGroupRequiredTitleRule(var message: String) : ManualInspectionGroupRule {
    override fun check(item: ManualInspectionGroup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionGroupSegmentLengthRule(var maxLength: Int) : ManualInspectionGroupRule {
    override fun check(item: ManualInspectionGroup): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionGroupCodeLengthRule(var maxLength: Int) : ManualInspectionGroupRule {
    override fun check(item: ManualInspectionGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionGroupCarrierLengthRule(var maxLength: Int) : ManualInspectionGroupRule {
    override fun check(item: ManualInspectionGroup): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionGroupRequiredCarrierRule(var message: String) : ManualInspectionGroupRule {
    override fun check(item: ManualInspectionGroup): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionGroupAllowedCodeRule(var allowed: MutableList<String>) : ManualInspectionGroupRule {
    override fun check(item: ManualInspectionGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual inspection group. */
data class ManualInspectionGroupValidator(var rules: MutableList<ManualInspectionGroupRule>) {
    fun validate(item: ManualInspectionGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualInspectionGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualInspectionGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultManualInspectionGroupValidator(): ManualInspectionGroupValidator {
    return ManualInspectionGroupValidator(
        mutableListOf<ManualInspectionGroupRule>(
            ManualInspectionGroupRequiredTitleRule("manual inspection group needs a value"),
            ManualInspectionGroupSegmentLengthRule(16),
            ManualInspectionGroupCodeLengthRule(1000),
            ManualInspectionGroupCarrierLengthRule(32),
            ManualInspectionGroupRequiredCarrierRule("manual inspection group needs an entry"),
            ManualInspectionGroupAllowedCodeRule(mutableListOf<String>("strict")),
        ),
    )
}
