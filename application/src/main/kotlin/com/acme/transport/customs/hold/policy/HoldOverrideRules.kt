package com.acme.transport.customs.hold.policy

/** A rule a hold override must pass. Check returns its problems, or none. */
interface HoldOverrideRule {
    fun check(item: HoldOverride): MutableList<String>
}

data class HoldOverrideRequiredCodeRule(var message: String) : HoldOverrideRule {
    override fun check(item: HoldOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldOverrideTitleLengthRule(var maxLength: Int) : HoldOverrideRule {
    override fun check(item: HoldOverride): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldOverrideCodeLengthRule(var maxLength: Int) : HoldOverrideRule {
    override fun check(item: HoldOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldOverrideContactEmailLengthRule(var maxLength: Int) : HoldOverrideRule {
    override fun check(item: HoldOverride): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldOverrideRequiredDepotRule(var message: String) : HoldOverrideRule {
    override fun check(item: HoldOverride): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold override. */
data class HoldOverrideValidator(var rules: MutableList<HoldOverrideRule>) {
    fun validate(item: HoldOverride): MutableList<String> {
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

    fun requireValid(item: HoldOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldOverrideValidator(): HoldOverrideValidator {
    return HoldOverrideValidator(
        mutableListOf<HoldOverrideRule>(
            HoldOverrideRequiredCodeRule("hold override needs a value"),
            HoldOverrideTitleLengthRule(16),
            HoldOverrideCodeLengthRule(32),
            HoldOverrideContactEmailLengthRule(16),
            HoldOverrideRequiredDepotRule("hold override needs an entry"),
        ),
    )
}
