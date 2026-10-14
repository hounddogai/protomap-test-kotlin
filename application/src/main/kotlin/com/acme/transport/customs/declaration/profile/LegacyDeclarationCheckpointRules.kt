package com.acme.transport.customs.declaration.profile

/** A rule a legacy declaration checkpoint must pass. Check returns its problems, or none. */
interface LegacyDeclarationCheckpointRule {
    fun check(item: LegacyDeclarationCheckpoint): MutableList<String>
}

data class LegacyDeclarationCheckpointDriverIdLengthRule(var maxLength: Int) : LegacyDeclarationCheckpointRule {
    override fun check(item: LegacyDeclarationCheckpoint): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationCheckpointRequiredCodeRule(var message: String) : LegacyDeclarationCheckpointRule {
    override fun check(item: LegacyDeclarationCheckpoint): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationCheckpointRequiredNameRule(var message: String) : LegacyDeclarationCheckpointRule {
    override fun check(item: LegacyDeclarationCheckpoint): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy declaration checkpoint. */
data class LegacyDeclarationCheckpointValidator(var rules: MutableList<LegacyDeclarationCheckpointRule>) {
    fun validate(item: LegacyDeclarationCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyDeclarationCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDeclarationCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDeclarationCheckpointValidator(): LegacyDeclarationCheckpointValidator {
    return LegacyDeclarationCheckpointValidator(
        mutableListOf<LegacyDeclarationCheckpointRule>(
            LegacyDeclarationCheckpointDriverIdLengthRule(255),
            LegacyDeclarationCheckpointRequiredCodeRule("legacy declaration checkpoint needs a value"),
            LegacyDeclarationCheckpointRequiredNameRule("legacy declaration checkpoint needs an entry"),
        ),
    )
}
