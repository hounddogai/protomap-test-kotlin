package com.acme.transport.linehaul.relay.notice

/** A rule a relay rollup must pass. Check returns its problems, or none. */
interface RelayRollupRule {
    fun check(item: RelayRollup): MutableList<String>
}

data class RelayRollupTitleLengthRule(var maxLength: Int) : RelayRollupRule {
    override fun check(item: RelayRollup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayRollupRequiredCodeRule(var message: String) : RelayRollupRule {
    override fun check(item: RelayRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayRollupCodePrefixRule(var prefix: String) : RelayRollupRule {
    override fun check(item: RelayRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RelayRollupRequiredDescriptionRule(var message: String) : RelayRollupRule {
    override fun check(item: RelayRollup): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayRollupRequiredTitleRule(var message: String) : RelayRollupRule {
    override fun check(item: RelayRollup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay rollup. */
data class RelayRollupValidator(var rules: MutableList<RelayRollupRule>) {
    fun validate(item: RelayRollup): MutableList<String> {
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

    fun requireValid(item: RelayRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayRollupValidator(): RelayRollupValidator {
    return RelayRollupValidator(
        mutableListOf<RelayRollupRule>(
            RelayRollupTitleLengthRule(16),
            RelayRollupRequiredCodeRule("relay rollup needs an entry"),
            RelayRollupCodePrefixRule("ACM"),
            RelayRollupRequiredDescriptionRule("relay rollup needs an entry"),
            RelayRollupRequiredTitleRule("relay rollup needs this field"),
        ),
    )
}
