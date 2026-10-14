package com.acme.transport.network.cutoff.regionalcutoffsegment

/** A rule a pending cutoff segment must pass. Check returns its problems, or none. */
interface PendingCutoffSegmentRule {
    fun check(item: PendingCutoffSegment): MutableList<String>
}

data class PendingCutoffSegmentNameLengthRule(var maxLength: Int) : PendingCutoffSegmentRule {
    override fun check(item: PendingCutoffSegment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffSegmentBudgetRangeRule(var min: Double, var max: Double) : PendingCutoffSegmentRule {
    override fun check(item: PendingCutoffSegment): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class PendingCutoffSegmentLocaleLengthRule(var maxLength: Int) : PendingCutoffSegmentRule {
    override fun check(item: PendingCutoffSegment): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCutoffSegmentRequiredSegmentRule(var message: String) : PendingCutoffSegmentRule {
    override fun check(item: PendingCutoffSegment): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending cutoff segment. */
data class PendingCutoffSegmentValidator(var rules: MutableList<PendingCutoffSegmentRule>) {
    fun validate(item: PendingCutoffSegment): MutableList<String> {
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

    fun requireValid(item: PendingCutoffSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCutoffSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCutoffSegmentValidator(): PendingCutoffSegmentValidator {
    return PendingCutoffSegmentValidator(
        mutableListOf<PendingCutoffSegmentRule>(
            PendingCutoffSegmentNameLengthRule(80),
            PendingCutoffSegmentBudgetRangeRule(5.5, 2149.9),
            PendingCutoffSegmentLocaleLengthRule(3600),
            PendingCutoffSegmentRequiredSegmentRule("pending cutoff segment needs an entry"),
        ),
    )
}
