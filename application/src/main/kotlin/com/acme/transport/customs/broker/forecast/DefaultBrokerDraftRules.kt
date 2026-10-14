package com.acme.transport.customs.broker.forecast

/** A rule a default broker draft must pass. Check returns its problems, or none. */
interface DefaultBrokerDraftRule {
    fun check(item: DefaultBrokerDraft): MutableList<String>
}

data class DefaultBrokerDraftDriverIdLengthRule(var maxLength: Int) : DefaultBrokerDraftRule {
    override fun check(item: DefaultBrokerDraft): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultBrokerDraftCodePrefixRule(var prefix: String) : DefaultBrokerDraftRule {
    override fun check(item: DefaultBrokerDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultBrokerDraftRequiredOwnerIdRule(var message: String) : DefaultBrokerDraftRule {
    override fun check(item: DefaultBrokerDraft): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultBrokerDraftRequiredCodeRule(var message: String) : DefaultBrokerDraftRule {
    override fun check(item: DefaultBrokerDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default broker draft. */
data class DefaultBrokerDraftValidator(var rules: MutableList<DefaultBrokerDraftRule>) {
    fun validate(item: DefaultBrokerDraft): MutableList<String> {
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

    fun requireValid(item: DefaultBrokerDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultBrokerDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultBrokerDraftValidator(): DefaultBrokerDraftValidator {
    return DefaultBrokerDraftValidator(
        mutableListOf<DefaultBrokerDraftRule>(
            DefaultBrokerDraftDriverIdLengthRule(255),
            DefaultBrokerDraftCodePrefixRule("ACM"),
            DefaultBrokerDraftRequiredOwnerIdRule("default broker draft needs a value"),
            DefaultBrokerDraftRequiredCodeRule("default broker draft needs a value"),
        ),
    )
}
