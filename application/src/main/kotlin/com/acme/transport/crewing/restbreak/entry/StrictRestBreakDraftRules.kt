package com.acme.transport.crewing.restbreak.entry

/** A rule a strict rest break draft must pass. Check returns its problems, or none. */
interface StrictRestBreakDraftRule {
    fun check(item: StrictRestBreakDraft): MutableList<String>
}

data class StrictRestBreakDraftSegmentLengthRule(var maxLength: Int) : StrictRestBreakDraftRule {
    override fun check(item: StrictRestBreakDraft): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakDraftRequiredCodeRule(var message: String) : StrictRestBreakDraftRule {
    override fun check(item: StrictRestBreakDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakDraftCodePrefixRule(var prefix: String) : StrictRestBreakDraftRule {
    override fun check(item: StrictRestBreakDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictRestBreakDraftCodeLengthRule(var maxLength: Int) : StrictRestBreakDraftRule {
    override fun check(item: StrictRestBreakDraft): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict rest break draft. */
data class StrictRestBreakDraftValidator(var rules: MutableList<StrictRestBreakDraftRule>) {
    fun validate(item: StrictRestBreakDraft): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRestBreakDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRestBreakDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRestBreakDraftValidator(): StrictRestBreakDraftValidator {
    return StrictRestBreakDraftValidator(
        mutableListOf<StrictRestBreakDraftRule>(
            StrictRestBreakDraftSegmentLengthRule(1000),
            StrictRestBreakDraftRequiredCodeRule("strict rest break draft needs this field"),
            StrictRestBreakDraftCodePrefixRule("REF"),
            StrictRestBreakDraftCodeLengthRule(120),
        ),
    )
}
