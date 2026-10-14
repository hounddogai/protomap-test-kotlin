package com.acme.transport.lastmile.attempt.batchedattemptrequest

/** A rule a pending attempt entry must pass. Check returns its problems, or none. */
interface PendingAttemptEntryRule {
    fun check(item: PendingAttemptEntry): MutableList<String>
}

data class PendingAttemptEntryRequiredNameRule(var message: String) : PendingAttemptEntryRule {
    override fun check(item: PendingAttemptEntry): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAttemptEntryRequiredShiftCodeRule(var message: String) : PendingAttemptEntryRule {
    override fun check(item: PendingAttemptEntry): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAttemptEntryRequiredCodeRule(var message: String) : PendingAttemptEntryRule {
    override fun check(item: PendingAttemptEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingAttemptEntryBudgetRangeRule(var min: Double, var max: Double) : PendingAttemptEntryRule {
    override fun check(item: PendingAttemptEntry): MutableList<String> {
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

data class PendingAttemptEntryNameLengthRule(var maxLength: Int) : PendingAttemptEntryRule {
    override fun check(item: PendingAttemptEntry): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingAttemptEntryCodePrefixRule(var prefix: String) : PendingAttemptEntryRule {
    override fun check(item: PendingAttemptEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending attempt entry. */
data class PendingAttemptEntryValidator(var rules: MutableList<PendingAttemptEntryRule>) {
    fun validate(item: PendingAttemptEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingAttemptEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingAttemptEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingAttemptEntryValidator(): PendingAttemptEntryValidator {
    return PendingAttemptEntryValidator(
        mutableListOf<PendingAttemptEntryRule>(
            PendingAttemptEntryRequiredNameRule("pending attempt entry needs an entry"),
            PendingAttemptEntryRequiredShiftCodeRule("pending attempt entry needs this field"),
            PendingAttemptEntryRequiredCodeRule("pending attempt entry needs an entry"),
            PendingAttemptEntryBudgetRangeRule(8.3, 4248.3),
            PendingAttemptEntryNameLengthRule(80),
            PendingAttemptEntryCodePrefixRule("REF"),
        ),
    )
}
