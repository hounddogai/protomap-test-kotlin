package com.acme.transport.customs.declaration.claim

/** A rule a default declaration record must pass. Check returns its problems, or none. */
interface DefaultDeclarationRecordRule {
    fun check(item: DefaultDeclarationRecord): MutableList<String>
}

data class DefaultDeclarationRecordCodePrefixRule(var prefix: String) : DefaultDeclarationRecordRule {
    override fun check(item: DefaultDeclarationRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDeclarationRecordRequiredTrailerIdRule(var message: String) : DefaultDeclarationRecordRule {
    override fun check(item: DefaultDeclarationRecord): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDeclarationRecordLabelLengthRule(var maxLength: Int) : DefaultDeclarationRecordRule {
    override fun check(item: DefaultDeclarationRecord): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default declaration record. */
data class DefaultDeclarationRecordValidator(var rules: MutableList<DefaultDeclarationRecordRule>) {
    fun validate(item: DefaultDeclarationRecord): MutableList<String> {
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

    fun requireValid(item: DefaultDeclarationRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDeclarationRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDeclarationRecordValidator(): DefaultDeclarationRecordValidator {
    return DefaultDeclarationRecordValidator(
        mutableListOf<DefaultDeclarationRecordRule>(
            DefaultDeclarationRecordCodePrefixRule("REF"),
            DefaultDeclarationRecordRequiredTrailerIdRule("default declaration record needs this field"),
            DefaultDeclarationRecordLabelLengthRule(32),
        ),
    )
}
