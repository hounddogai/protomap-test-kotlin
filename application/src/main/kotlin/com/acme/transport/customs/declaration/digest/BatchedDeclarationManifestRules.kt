package com.acme.transport.customs.declaration.digest

/** A rule a batched declaration manifest must pass. Check returns its problems, or none. */
interface BatchedDeclarationManifestRule {
    fun check(item: BatchedDeclarationManifest): MutableList<String>
}

data class BatchedDeclarationManifestRequiredDockRule(var message: String) : BatchedDeclarationManifestRule {
    override fun check(item: BatchedDeclarationManifest): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedDeclarationManifestCodeLengthRule(var maxLength: Int) : BatchedDeclarationManifestRule {
    override fun check(item: BatchedDeclarationManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDeclarationManifestActiveGuardRule(var limit: Double) : BatchedDeclarationManifestRule {
    override fun check(item: BatchedDeclarationManifest): MutableList<String> {
        if (item.active && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDeclarationManifestNameLengthRule(var maxLength: Int) : BatchedDeclarationManifestRule {
    override fun check(item: BatchedDeclarationManifest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedDeclarationManifestChannelLengthRule(var maxLength: Int) : BatchedDeclarationManifestRule {
    override fun check(item: BatchedDeclarationManifest): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched declaration manifest. */
data class BatchedDeclarationManifestValidator(var rules: MutableList<BatchedDeclarationManifestRule>) {
    fun validate(item: BatchedDeclarationManifest): MutableList<String> {
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

    fun requireValid(item: BatchedDeclarationManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedDeclarationManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedDeclarationManifestValidator(): BatchedDeclarationManifestValidator {
    return BatchedDeclarationManifestValidator(
        mutableListOf<BatchedDeclarationManifestRule>(
            BatchedDeclarationManifestRequiredDockRule("batched declaration manifest needs an entry"),
            BatchedDeclarationManifestCodeLengthRule(32),
            BatchedDeclarationManifestActiveGuardRule(9.4),
            BatchedDeclarationManifestNameLengthRule(255),
            BatchedDeclarationManifestChannelLengthRule(86400),
        ),
    )
}
