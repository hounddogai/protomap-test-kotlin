package com.acme.transport.customs.declaration.segment

/** A rule a batched declaration digest must pass. Check returns its problems, or none. */
interface BatchedDeclarationDigestRule {
    fun check(item: BatchedDeclarationDigest): MutableList<String>
}

data class BatchedDeclarationDigestCodePrefixRule(var prefix: String) : BatchedDeclarationDigestRule {
    override fun check(item: BatchedDeclarationDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDeclarationDigestRequiredLabelRule(var message: String) : BatchedDeclarationDigestRule {
    override fun check(item: BatchedDeclarationDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDeclarationDigestRequiredLocaleRule(var message: String) : BatchedDeclarationDigestRule {
    override fun check(item: BatchedDeclarationDigest): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched declaration digest. */
data class BatchedDeclarationDigestValidator(var rules: MutableList<BatchedDeclarationDigestRule>) {
    fun validate(item: BatchedDeclarationDigest): MutableList<String> {
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

    fun requireValid(item: BatchedDeclarationDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDeclarationDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDeclarationDigestValidator(): BatchedDeclarationDigestValidator {
    return BatchedDeclarationDigestValidator(
        mutableListOf<BatchedDeclarationDigestRule>(
            BatchedDeclarationDigestCodePrefixRule("REF"),
            BatchedDeclarationDigestRequiredLabelRule("batched declaration digest needs an entry"),
            BatchedDeclarationDigestRequiredLocaleRule("batched declaration digest needs this field"),
        ),
    )
}
