package com.acme.transport.customs.broker.entry

/** A rule a primary broker entry must pass. Check returns its problems, or none. */
interface PrimaryBrokerEntryRule {
    fun check(item: PrimaryBrokerEntry): MutableList<String>
}

data class PrimaryBrokerEntryCodeLengthRule(var maxLength: Int) : PrimaryBrokerEntryRule {
    override fun check(item: PrimaryBrokerEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBrokerEntryCodePrefixRule(var prefix: String) : PrimaryBrokerEntryRule {
    override fun check(item: PrimaryBrokerEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBrokerEntryPriorityRangeRule(var min: Int, var max: Int) : PrimaryBrokerEntryRule {
    override fun check(item: PrimaryBrokerEntry): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class PrimaryBrokerEntryRequiredCodeRule(var message: String) : PrimaryBrokerEntryRule {
    override fun check(item: PrimaryBrokerEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary broker entry. */
data class PrimaryBrokerEntryValidator(var rules: MutableList<PrimaryBrokerEntryRule>) {
    fun validate(item: PrimaryBrokerEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryBrokerEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryBrokerEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryBrokerEntryValidator(): PrimaryBrokerEntryValidator {
    return PrimaryBrokerEntryValidator(
        mutableListOf<PrimaryBrokerEntryRule>(
            PrimaryBrokerEntryCodeLengthRule(80),
            PrimaryBrokerEntryCodePrefixRule("REF"),
            PrimaryBrokerEntryPriorityRangeRule(64, 86400),
            PrimaryBrokerEntryRequiredCodeRule("primary broker entry needs an entry"),
        ),
    )
}
