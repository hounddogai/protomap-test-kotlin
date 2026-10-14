package com.acme.transport.customs.broker.audit

/** A rule a manual broker override must pass. Check returns its problems, or none. */
interface ManualBrokerOverrideRule {
    fun check(item: ManualBrokerOverride): MutableList<String>
}

data class ManualBrokerOverridePalletsRangeRule(var min: Int, var max: Int) : ManualBrokerOverrideRule {
    override fun check(item: ManualBrokerOverride): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class ManualBrokerOverrideCodePrefixRule(var prefix: String) : ManualBrokerOverrideRule {
    override fun check(item: ManualBrokerOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualBrokerOverrideRequiredCodeRule(var message: String) : ManualBrokerOverrideRule {
    override fun check(item: ManualBrokerOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual broker override. */
data class ManualBrokerOverrideValidator(var rules: MutableList<ManualBrokerOverrideRule>) {
    fun validate(item: ManualBrokerOverride): MutableList<String> {
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

    fun requireValid(item: ManualBrokerOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualBrokerOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultManualBrokerOverrideValidator(): ManualBrokerOverrideValidator {
    return ManualBrokerOverrideValidator(
        mutableListOf<ManualBrokerOverrideRule>(
            ManualBrokerOverridePalletsRangeRule(16, 32),
            ManualBrokerOverrideCodePrefixRule("OPS"),
            ManualBrokerOverrideRequiredCodeRule("manual broker override needs this field"),
        ),
    )
}
