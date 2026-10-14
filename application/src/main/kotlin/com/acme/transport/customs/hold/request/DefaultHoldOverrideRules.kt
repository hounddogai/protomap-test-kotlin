package com.acme.transport.customs.hold.request

/** A rule a default hold override must pass. Check returns its problems, or none. */
interface DefaultHoldOverrideRule {
    fun check(item: DefaultHoldOverride): MutableList<String>
}

data class DefaultHoldOverrideRequiredLabelRule(var message: String) : DefaultHoldOverrideRule {
    override fun check(item: DefaultHoldOverride): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultHoldOverrideRequiredCodeRule(var message: String) : DefaultHoldOverrideRule {
    override fun check(item: DefaultHoldOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultHoldOverrideLabelLengthRule(var maxLength: Int) : DefaultHoldOverrideRule {
    override fun check(item: DefaultHoldOverride): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default hold override. */
data class DefaultHoldOverrideValidator(var rules: MutableList<DefaultHoldOverrideRule>) {
    fun validate(item: DefaultHoldOverride): MutableList<String> {
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

    fun requireValid(item: DefaultHoldOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultHoldOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultHoldOverrideValidator(): DefaultHoldOverrideValidator {
    return DefaultHoldOverrideValidator(
        mutableListOf<DefaultHoldOverrideRule>(
            DefaultHoldOverrideRequiredLabelRule("default hold override needs this field"),
            DefaultHoldOverrideRequiredCodeRule("default hold override needs an entry"),
            DefaultHoldOverrideLabelLengthRule(86400),
        ),
    )
}
