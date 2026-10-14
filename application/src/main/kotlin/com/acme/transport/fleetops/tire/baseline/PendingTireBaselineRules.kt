package com.acme.transport.fleetops.tire.baseline

/** A rule a pending tire baseline must pass. Check returns its problems, or none. */
interface PendingTireBaselineRule {
    fun check(item: PendingTireBaseline): MutableList<String>
}

data class PendingTireBaselineRequiredNotesRule(var message: String) : PendingTireBaselineRule {
    override fun check(item: PendingTireBaseline): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTireBaselineRequiredSegmentRule(var message: String) : PendingTireBaselineRule {
    override fun check(item: PendingTireBaseline): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingTireBaselineLabelLengthRule(var maxLength: Int) : PendingTireBaselineRule {
    override fun check(item: PendingTireBaseline): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending tire baseline. */
data class PendingTireBaselineValidator(var rules: MutableList<PendingTireBaselineRule>) {
    fun validate(item: PendingTireBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingTireBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingTireBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingTireBaselineValidator(): PendingTireBaselineValidator {
    return PendingTireBaselineValidator(
        mutableListOf<PendingTireBaselineRule>(
            PendingTireBaselineRequiredNotesRule("pending tire baseline needs a value"),
            PendingTireBaselineRequiredSegmentRule("pending tire baseline needs this field"),
            PendingTireBaselineLabelLengthRule(32),
        ),
    )
}
