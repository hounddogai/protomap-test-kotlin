package com.acme.transport.customs.inspection.inspectiontarget

/** A rule a batched inspection claim must pass. Check returns its problems, or none. */
interface BatchedInspectionClaimRule {
    fun check(item: BatchedInspectionClaim): MutableList<String>
}

data class BatchedInspectionClaimRequiredNameRule(var message: String) : BatchedInspectionClaimRule {
    override fun check(item: BatchedInspectionClaim): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionClaimNameLengthRule(var maxLength: Int) : BatchedInspectionClaimRule {
    override fun check(item: BatchedInspectionClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionClaimCodePrefixRule(var prefix: String) : BatchedInspectionClaimRule {
    override fun check(item: BatchedInspectionClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedInspectionClaimCodeLengthRule(var maxLength: Int) : BatchedInspectionClaimRule {
    override fun check(item: BatchedInspectionClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched inspection claim. */
data class BatchedInspectionClaimValidator(var rules: MutableList<BatchedInspectionClaimRule>) {
    fun validate(item: BatchedInspectionClaim): MutableList<String> {
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

    fun requireValid(item: BatchedInspectionClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedInspectionClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedInspectionClaimValidator(): BatchedInspectionClaimValidator {
    return BatchedInspectionClaimValidator(
        mutableListOf<BatchedInspectionClaimRule>(
            BatchedInspectionClaimRequiredNameRule("batched inspection claim needs a value"),
            BatchedInspectionClaimNameLengthRule(80),
            BatchedInspectionClaimCodePrefixRule("Q"),
            BatchedInspectionClaimCodeLengthRule(86400),
        ),
    )
}
