package com.acme.transport.network.hub.proposal

/** A rule a default hub entry must pass. Check returns its problems, or none. */
interface DefaultHubEntryRule {
    fun check(item: DefaultHubEntry): MutableList<String>
}

data class DefaultHubEntryChannelLengthRule(var maxLength: Int) : DefaultHubEntryRule {
    override fun check(item: DefaultHubEntry): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubEntryRequiredChannelRule(var message: String) : DefaultHubEntryRule {
    override fun check(item: DefaultHubEntry): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultHubEntryRequiredPhoneRule(var message: String) : DefaultHubEntryRule {
    override fun check(item: DefaultHubEntry): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default hub entry. */
data class DefaultHubEntryValidator(var rules: MutableList<DefaultHubEntryRule>) {
    fun validate(item: DefaultHubEntry): MutableList<String> {
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

    fun requireValid(item: DefaultHubEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultHubEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultHubEntryValidator(): DefaultHubEntryValidator {
    return DefaultHubEntryValidator(
        mutableListOf<DefaultHubEntryRule>(
            DefaultHubEntryChannelLengthRule(16),
            DefaultHubEntryRequiredChannelRule("default hub entry needs this field"),
            DefaultHubEntryRequiredPhoneRule("default hub entry needs this field"),
        ),
    )
}
