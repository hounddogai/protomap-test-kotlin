package com.acme.transport.crewing.roster.entry

/** A rule a batched roster cycle must pass. Check returns its problems, or none. */
interface BatchedRosterCycleRule {
    fun check(item: BatchedRosterCycle): MutableList<String>
}

data class BatchedRosterCycleContactEmailLengthRule(var maxLength: Int) : BatchedRosterCycleRule {
    override fun check(item: BatchedRosterCycle): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterCycleRequiredCodeRule(var message: String) : BatchedRosterCycleRule {
    override fun check(item: BatchedRosterCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedRosterCycleCodePrefixRule(var prefix: String) : BatchedRosterCycleRule {
    override fun check(item: BatchedRosterCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched roster cycle. */
data class BatchedRosterCycleValidator(var rules: MutableList<BatchedRosterCycleRule>) {
    fun validate(item: BatchedRosterCycle): MutableList<String> {
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

    fun requireValid(item: BatchedRosterCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedRosterCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedRosterCycleValidator(): BatchedRosterCycleValidator {
    return BatchedRosterCycleValidator(
        mutableListOf<BatchedRosterCycleRule>(
            BatchedRosterCycleContactEmailLengthRule(64),
            BatchedRosterCycleRequiredCodeRule("batched roster cycle needs a value"),
            BatchedRosterCycleCodePrefixRule("ACM"),
        ),
    )
}
