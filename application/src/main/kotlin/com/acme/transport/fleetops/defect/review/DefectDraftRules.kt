package com.acme.transport.fleetops.defect.review

/** A rule a defect draft must pass. Check returns its problems, or none. */
interface DefectDraftRule {
    fun check(item: DefectDraft): MutableList<String>
}

data class DefectDraftCodePrefixRule(var prefix: String) : DefectDraftRule {
    override fun check(item: DefectDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefectDraftCodeLengthRule(var maxLength: Int) : DefectDraftRule {
    override fun check(item: DefectDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectDraftRequiredSegmentRule(var message: String) : DefectDraftRule {
    override fun check(item: DefectDraft): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect draft. */
data class DefectDraftValidator(var rules: MutableList<DefectDraftRule>) {
    fun validate(item: DefectDraft): MutableList<String> {
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

    fun requireValid(item: DefectDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectDraftValidator(): DefectDraftValidator {
    return DefectDraftValidator(
        mutableListOf<DefectDraftRule>(
            DefectDraftCodePrefixRule("OPS"),
            DefectDraftCodeLengthRule(16),
            DefectDraftRequiredSegmentRule("defect draft needs an entry"),
        ),
    )
}
