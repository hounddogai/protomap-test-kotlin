package com.acme.transport.fleetops.inspection.overrideitem

/** A rule a pending inspection override must pass. Check returns its problems, or none. */
interface PendingInspectionOverrideRule {
    fun check(item: PendingInspectionOverride): MutableList<String>
}

data class PendingInspectionOverrideRequiredSummaryRule(var message: String) : PendingInspectionOverrideRule {
    override fun check(item: PendingInspectionOverride): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionOverrideSequenceRangeRule(
    var min: Int,
    var max: Int,
) : PendingInspectionOverrideRule {
    override fun check(item: PendingInspectionOverride): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionOverrideRequiredTitleRule(var message: String) : PendingInspectionOverrideRule {
    override fun check(item: PendingInspectionOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingInspectionOverrideCodePrefixRule(var prefix: String) : PendingInspectionOverrideRule {
    override fun check(item: PendingInspectionOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending inspection override. */
data class PendingInspectionOverrideValidator(var rules: MutableList<PendingInspectionOverrideRule>) {
    fun validate(item: PendingInspectionOverride): MutableList<String> {
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

    fun requireValid(item: PendingInspectionOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingInspectionOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingInspectionOverrideValidator(): PendingInspectionOverrideValidator {
    return PendingInspectionOverrideValidator(
        mutableListOf<PendingInspectionOverrideRule>(
            PendingInspectionOverrideRequiredSummaryRule("pending inspection override needs an entry"),
            PendingInspectionOverrideSequenceRangeRule(16, 64),
            PendingInspectionOverrideRequiredTitleRule("pending inspection override needs this field"),
            PendingInspectionOverrideCodePrefixRule("ACM"),
        ),
    )
}
