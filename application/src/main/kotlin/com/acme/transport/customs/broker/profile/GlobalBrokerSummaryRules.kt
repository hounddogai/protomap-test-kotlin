package com.acme.transport.customs.broker.profile

/** A rule a global broker summary must pass. Check returns its problems, or none. */
interface GlobalBrokerSummaryRule {
    fun check(item: GlobalBrokerSummary): MutableList<String>
}

data class GlobalBrokerSummaryCodePrefixRule(var prefix: String) : GlobalBrokerSummaryRule {
    override fun check(item: GlobalBrokerSummary): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerSummarySourceLengthRule(var maxLength: Int) : GlobalBrokerSummaryRule {
    override fun check(item: GlobalBrokerSummary): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerSummaryRequiredSourceRule(var message: String) : GlobalBrokerSummaryRule {
    override fun check(item: GlobalBrokerSummary): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalBrokerSummaryRequiredCodeRule(var message: String) : GlobalBrokerSummaryRule {
    override fun check(item: GlobalBrokerSummary): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global broker summary. */
data class GlobalBrokerSummaryValidator(var rules: MutableList<GlobalBrokerSummaryRule>) {
    fun validate(item: GlobalBrokerSummary): MutableList<String> {
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

    fun requireValid(item: GlobalBrokerSummary) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalBrokerSummary>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalBrokerSummaryValidator(): GlobalBrokerSummaryValidator {
    return GlobalBrokerSummaryValidator(
        mutableListOf<GlobalBrokerSummaryRule>(
            GlobalBrokerSummaryCodePrefixRule("Q"),
            GlobalBrokerSummarySourceLengthRule(16),
            GlobalBrokerSummaryRequiredSourceRule("global broker summary needs this field"),
            GlobalBrokerSummaryRequiredCodeRule("global broker summary needs a value"),
        ),
    )
}
