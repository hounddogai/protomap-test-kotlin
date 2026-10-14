package com.acme.transport.terminal.sortation.sortationbaseline

/** A rule a regional sortation override must pass. Check returns its problems, or none. */
interface RegionalSortationOverrideRule {
    fun check(item: RegionalSortationOverride): MutableList<String>
}

data class RegionalSortationOverrideRequiredNameRule(var message: String) : RegionalSortationOverrideRule {
    override fun check(item: RegionalSortationOverride): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationOverrideCodePrefixRule(var prefix: String) : RegionalSortationOverrideRule {
    override fun check(item: RegionalSortationOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationOverrideRequiredCodeRule(var message: String) : RegionalSortationOverrideRule {
    override fun check(item: RegionalSortationOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalSortationOverrideNameLengthRule(var maxLength: Int) : RegionalSortationOverrideRule {
    override fun check(item: RegionalSortationOverride): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional sortation override. */
data class RegionalSortationOverrideValidator(var rules: MutableList<RegionalSortationOverrideRule>) {
    fun validate(item: RegionalSortationOverride): MutableList<String> {
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

    fun requireValid(item: RegionalSortationOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalSortationOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalSortationOverrideValidator(): RegionalSortationOverrideValidator {
    return RegionalSortationOverrideValidator(
        mutableListOf<RegionalSortationOverrideRule>(
            RegionalSortationOverrideRequiredNameRule("regional sortation override needs a value"),
            RegionalSortationOverrideCodePrefixRule("Q"),
            RegionalSortationOverrideRequiredCodeRule("regional sortation override needs this field"),
            RegionalSortationOverrideNameLengthRule(80),
        ),
    )
}
