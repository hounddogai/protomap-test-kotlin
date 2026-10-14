package com.acme.transport.linehaul.backhaul.backhaulassignment

/** A rule a batched backhaul assignment must pass. Check returns its problems, or none. */
interface BatchedBackhaulAssignmentRule {
    fun check(item: BatchedBackhaulAssignment): MutableList<String>
}

data class BatchedBackhaulAssignmentCodePrefixRule(var prefix: String) : BatchedBackhaulAssignmentRule {
    override fun check(item: BatchedBackhaulAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedBackhaulAssignmentRequiredTrailerIdRule(var message: String) : BatchedBackhaulAssignmentRule {
    override fun check(item: BatchedBackhaulAssignment): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedBackhaulAssignmentRequiredCodeRule(var message: String) : BatchedBackhaulAssignmentRule {
    override fun check(item: BatchedBackhaulAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched backhaul assignment. */
data class BatchedBackhaulAssignmentValidator(var rules: MutableList<BatchedBackhaulAssignmentRule>) {
    fun validate(item: BatchedBackhaulAssignment): MutableList<String> {
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

    fun requireValid(item: BatchedBackhaulAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedBackhaulAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedBackhaulAssignmentValidator(): BatchedBackhaulAssignmentValidator {
    return BatchedBackhaulAssignmentValidator(
        mutableListOf<BatchedBackhaulAssignmentRule>(
            BatchedBackhaulAssignmentCodePrefixRule("OPS"),
            BatchedBackhaulAssignmentRequiredTrailerIdRule("batched backhaul assignment needs a value"),
            BatchedBackhaulAssignmentRequiredCodeRule("batched backhaul assignment needs an entry"),
        ),
    )
}
