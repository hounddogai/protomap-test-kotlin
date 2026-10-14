package com.acme.transport.network.node.bundle

/** A rule a scheduled node profile must pass. Check returns its problems, or none. */
interface ScheduledNodeProfileRule {
    fun check(item: ScheduledNodeProfile): MutableList<String>
}

data class ScheduledNodeProfileReviewerLengthRule(var maxLength: Int) : ScheduledNodeProfileRule {
    override fun check(item: ScheduledNodeProfile): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNodeProfileRequiredLabelRule(var message: String) : ScheduledNodeProfileRule {
    override fun check(item: ScheduledNodeProfile): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledNodeProfileLabelLengthRule(var maxLength: Int) : ScheduledNodeProfileRule {
    override fun check(item: ScheduledNodeProfile): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled node profile. */
data class ScheduledNodeProfileValidator(var rules: MutableList<ScheduledNodeProfileRule>) {
    fun validate(item: ScheduledNodeProfile): MutableList<String> {
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

    fun requireValid(item: ScheduledNodeProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledNodeProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledNodeProfileValidator(): ScheduledNodeProfileValidator {
    return ScheduledNodeProfileValidator(
        mutableListOf<ScheduledNodeProfileRule>(
            ScheduledNodeProfileReviewerLengthRule(80),
            ScheduledNodeProfileRequiredLabelRule("scheduled node profile needs an entry"),
            ScheduledNodeProfileLabelLengthRule(120),
        ),
    )
}
