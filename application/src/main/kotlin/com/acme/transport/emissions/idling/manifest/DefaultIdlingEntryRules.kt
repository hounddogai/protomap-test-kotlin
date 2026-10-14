package com.acme.transport.emissions.idling.manifest

/** A rule a default idling entry must pass. Check returns its problems, or none. */
interface DefaultIdlingEntryRule {
    fun check(item: DefaultIdlingEntry): MutableList<String>
}

data class DefaultIdlingEntrySourceLengthRule(var maxLength: Int) : DefaultIdlingEntryRule {
    override fun check(item: DefaultIdlingEntry): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultIdlingEntryNameLengthRule(var maxLength: Int) : DefaultIdlingEntryRule {
    override fun check(item: DefaultIdlingEntry): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultIdlingEntryRequiredCodeRule(var message: String) : DefaultIdlingEntryRule {
    override fun check(item: DefaultIdlingEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultIdlingEntryRequiredChecksumRule(var message: String) : DefaultIdlingEntryRule {
    override fun check(item: DefaultIdlingEntry): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default idling entry. */
data class DefaultIdlingEntryValidator(var rules: MutableList<DefaultIdlingEntryRule>) {
    fun validate(item: DefaultIdlingEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultIdlingEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultIdlingEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultIdlingEntryValidator(): DefaultIdlingEntryValidator {
    return DefaultIdlingEntryValidator(
        mutableListOf<DefaultIdlingEntryRule>(
            DefaultIdlingEntrySourceLengthRule(120),
            DefaultIdlingEntryNameLengthRule(16),
            DefaultIdlingEntryRequiredCodeRule("default idling entry needs an entry"),
            DefaultIdlingEntryRequiredChecksumRule("default idling entry needs this field"),
        ),
    )
}
