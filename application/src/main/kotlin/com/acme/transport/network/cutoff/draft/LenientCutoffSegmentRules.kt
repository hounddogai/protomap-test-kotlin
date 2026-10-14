package com.acme.transport.network.cutoff.draft

/** A rule a lenient cutoff segment must pass. Check returns its problems, or none. */
interface LenientCutoffSegmentRule {
    fun check(item: LenientCutoffSegment): MutableList<String>
}

data class LenientCutoffSegmentRequiredReviewerRule(var message: String) : LenientCutoffSegmentRule {
    override fun check(item: LenientCutoffSegment): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCutoffSegmentLabelLengthRule(var maxLength: Int) : LenientCutoffSegmentRule {
    override fun check(item: LenientCutoffSegment): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCutoffSegmentCodeLengthRule(var maxLength: Int) : LenientCutoffSegmentRule {
    override fun check(item: LenientCutoffSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCutoffSegmentCodePrefixRule(var prefix: String) : LenientCutoffSegmentRule {
    override fun check(item: LenientCutoffSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient cutoff segment. */
data class LenientCutoffSegmentValidator(var rules: MutableList<LenientCutoffSegmentRule>) {
    fun validate(item: LenientCutoffSegment): MutableList<String> {
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

    fun requireValid(item: LenientCutoffSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCutoffSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCutoffSegmentValidator(): LenientCutoffSegmentValidator {
    return LenientCutoffSegmentValidator(
        mutableListOf<LenientCutoffSegmentRule>(
            LenientCutoffSegmentRequiredReviewerRule("lenient cutoff segment needs a value"),
            LenientCutoffSegmentLabelLengthRule(3600),
            LenientCutoffSegmentCodeLengthRule(86400),
            LenientCutoffSegmentCodePrefixRule("Q"),
        ),
    )
}
