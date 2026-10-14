package com.acme.transport.network.node.assignment

import com.acme.transport.kit.text.normalize

/** A rule a batched node assignment must pass. Check returns its problems, or none. */
interface BatchedNodeAssignmentRule {
    fun check(item: BatchedNodeAssignment): MutableList<String>
}

data class BatchedNodeAssignmentAllowedTitleRule(var allowed: MutableList<String>) : BatchedNodeAssignmentRule {
    override fun check(item: BatchedNodeAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeAssignmentTitleLengthRule(var maxLength: Int) : BatchedNodeAssignmentRule {
    override fun check(item: BatchedNodeAssignment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeAssignmentRequiredTitleRule(var message: String) : BatchedNodeAssignmentRule {
    override fun check(item: BatchedNodeAssignment): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedNodeAssignmentRequiredContactEmailRule(var message: String) : BatchedNodeAssignmentRule {
    override fun check(item: BatchedNodeAssignment): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched node assignment. */
data class BatchedNodeAssignmentValidator(var rules: MutableList<BatchedNodeAssignmentRule>) {
    fun validate(item: BatchedNodeAssignment): MutableList<String> {
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

    fun requireValid(item: BatchedNodeAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedNodeAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedNodeAssignmentValidator(): BatchedNodeAssignmentValidator {
    return BatchedNodeAssignmentValidator(
        mutableListOf<BatchedNodeAssignmentRule>(
            BatchedNodeAssignmentAllowedTitleRule(mutableListOf<String>("global", "pending")),
            BatchedNodeAssignmentTitleLengthRule(255),
            BatchedNodeAssignmentRequiredTitleRule("batched node assignment needs a value"),
            BatchedNodeAssignmentRequiredContactEmailRule("batched node assignment needs a value"),
        ),
    )
}
