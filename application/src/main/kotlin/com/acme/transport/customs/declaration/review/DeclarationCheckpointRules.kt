package com.acme.transport.customs.declaration.review

/** A rule a declaration checkpoint must pass. Check returns its problems, or none. */
interface DeclarationCheckpointRule {
    fun check(item: DeclarationCheckpoint): MutableList<String>
}

data class DeclarationCheckpointRequiredCodeRule(var message: String) : DeclarationCheckpointRule {
    override fun check(item: DeclarationCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationCheckpointCodeLengthRule(var maxLength: Int) : DeclarationCheckpointRule {
    override fun check(item: DeclarationCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationCheckpointRequiredTitleRule(var message: String) : DeclarationCheckpointRule {
    override fun check(item: DeclarationCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration checkpoint. */
data class DeclarationCheckpointValidator(var rules: MutableList<DeclarationCheckpointRule>) {
    fun validate(item: DeclarationCheckpoint): MutableList<String> {
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

    fun requireValid(item: DeclarationCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationCheckpointValidator(): DeclarationCheckpointValidator {
    return DeclarationCheckpointValidator(
        mutableListOf<DeclarationCheckpointRule>(
            DeclarationCheckpointRequiredCodeRule("declaration checkpoint needs this field"),
            DeclarationCheckpointCodeLengthRule(1000),
            DeclarationCheckpointRequiredTitleRule("declaration checkpoint needs this field"),
        ),
    )
}
