package com.acme.transport.network.hub.mapping

/** A rule a hub ledger must pass. Check returns its problems, or none. */
interface HubLedgerRule {
    fun check(item: HubLedger): MutableList<String>
}

data class HubLedgerCodePrefixRule(var prefix: String) : HubLedgerRule {
    override fun check(item: HubLedger): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HubLedgerPhoneLengthRule(var maxLength: Int) : HubLedgerRule {
    override fun check(item: HubLedger): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubLedgerNameLengthRule(var maxLength: Int) : HubLedgerRule {
    override fun check(item: HubLedger): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubLedgerRequiredChannelRule(var message: String) : HubLedgerRule {
    override fun check(item: HubLedger): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HubLedgerWeightRangeRule(var min: Double, var max: Double) : HubLedgerRule {
    override fun check(item: HubLedger): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a hub ledger. */
data class HubLedgerValidator(var rules: MutableList<HubLedgerRule>) {
    fun validate(item: HubLedger): MutableList<String> {
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

    fun requireValid(item: HubLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultHubLedgerValidator(): HubLedgerValidator {
    return HubLedgerValidator(
        mutableListOf<HubLedgerRule>(
            HubLedgerCodePrefixRule("ACM"),
            HubLedgerPhoneLengthRule(120),
            HubLedgerNameLengthRule(86400),
            HubLedgerRequiredChannelRule("hub ledger needs an entry"),
            HubLedgerWeightRangeRule(1.2, 2376.8),
        ),
    )
}
