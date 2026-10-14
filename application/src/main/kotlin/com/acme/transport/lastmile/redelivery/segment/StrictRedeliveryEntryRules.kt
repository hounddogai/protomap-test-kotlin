package com.acme.transport.lastmile.redelivery.segment

/** A rule a strict redelivery entry must pass. Check returns its problems, or none. */
interface StrictRedeliveryEntryRule {
    fun check(item: StrictRedeliveryEntry): MutableList<String>
}

data class StrictRedeliveryEntryCodeLengthRule(var maxLength: Int) : StrictRedeliveryEntryRule {
    override fun check(item: StrictRedeliveryEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRedeliveryEntryRequiredSourceRule(var message: String) : StrictRedeliveryEntryRule {
    override fun check(item: StrictRedeliveryEntry): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRedeliveryEntrySourceLengthRule(var maxLength: Int) : StrictRedeliveryEntryRule {
    override fun check(item: StrictRedeliveryEntry): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictRedeliveryEntryExternalRefLengthRule(var maxLength: Int) : StrictRedeliveryEntryRule {
    override fun check(item: StrictRedeliveryEntry): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict redelivery entry. */
data class StrictRedeliveryEntryValidator(var rules: MutableList<StrictRedeliveryEntryRule>) {
    fun validate(item: StrictRedeliveryEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRedeliveryEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRedeliveryEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRedeliveryEntryValidator(): StrictRedeliveryEntryValidator {
    return StrictRedeliveryEntryValidator(
        mutableListOf<StrictRedeliveryEntryRule>(
            StrictRedeliveryEntryCodeLengthRule(32),
            StrictRedeliveryEntryRequiredSourceRule("strict redelivery entry needs a value"),
            StrictRedeliveryEntrySourceLengthRule(16),
            StrictRedeliveryEntryExternalRefLengthRule(1000),
        ),
    )
}
