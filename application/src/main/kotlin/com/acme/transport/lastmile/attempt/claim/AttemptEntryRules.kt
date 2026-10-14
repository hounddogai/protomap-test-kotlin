package com.acme.transport.lastmile.attempt.claim

/** A rule a attempt entry must pass. Check returns its problems, or none. */
interface AttemptEntryRule {
    fun check(item: AttemptEntry): MutableList<String>
}

data class AttemptEntryChannelLengthRule(var maxLength: Int) : AttemptEntryRule {
    override fun check(item: AttemptEntry): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptEntryUnitLengthRule(var maxLength: Int) : AttemptEntryRule {
    override fun check(item: AttemptEntry): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptEntryLabelLengthRule(var maxLength: Int) : AttemptEntryRule {
    override fun check(item: AttemptEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptEntryCodeLengthRule(var maxLength: Int) : AttemptEntryRule {
    override fun check(item: AttemptEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptEntryCodePrefixRule(var prefix: String) : AttemptEntryRule {
    override fun check(item: AttemptEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt entry. */
data class AttemptEntryValidator(var rules: MutableList<AttemptEntryRule>) {
    fun validate(item: AttemptEntry): MutableList<String> {
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

    fun requireValid(item: AttemptEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptEntryValidator(): AttemptEntryValidator {
    return AttemptEntryValidator(
        mutableListOf<AttemptEntryRule>(
            AttemptEntryChannelLengthRule(80),
            AttemptEntryUnitLengthRule(3600),
            AttemptEntryLabelLengthRule(3600),
            AttemptEntryCodeLengthRule(86400),
            AttemptEntryCodePrefixRule("ACM"),
        ),
    )
}
