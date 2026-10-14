package com.acme.transport.network.cutoff.draft

/** A rule a cutoff draft must pass. Check returns its problems, or none. */
interface CutoffDraftRule {
    fun check(item: CutoffDraft): MutableList<String>
}

data class CutoffDraftApprovedGuardRule(var limit: Double) : CutoffDraftRule {
    override fun check(item: CutoffDraft): MutableList<String> {
        if (item.approved && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class CutoffDraftSegmentLengthRule(var maxLength: Int) : CutoffDraftRule {
    override fun check(item: CutoffDraft): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffDraftRequiredLabelRule(var message: String) : CutoffDraftRule {
    override fun check(item: CutoffDraft): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffDraftRouteCodeLengthRule(var maxLength: Int) : CutoffDraftRule {
    override fun check(item: CutoffDraft): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffDraftScoreRangeRule(var min: Double, var max: Double) : CutoffDraftRule {
    override fun check(item: CutoffDraft): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CutoffDraftCodePrefixRule(var prefix: String) : CutoffDraftRule {
    override fun check(item: CutoffDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff draft. */
data class CutoffDraftValidator(var rules: MutableList<CutoffDraftRule>) {
    fun validate(item: CutoffDraft): MutableList<String> {
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

    fun requireValid(item: CutoffDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffDraftValidator(): CutoffDraftValidator {
    return CutoffDraftValidator(
        mutableListOf<CutoffDraftRule>(
            CutoffDraftApprovedGuardRule(3.5),
            CutoffDraftSegmentLengthRule(3600),
            CutoffDraftRequiredLabelRule("cutoff draft needs this field"),
            CutoffDraftRouteCodeLengthRule(86400),
            CutoffDraftScoreRangeRule(7.4, 4250.0),
            CutoffDraftCodePrefixRule("OPS"),
        ),
    )
}
