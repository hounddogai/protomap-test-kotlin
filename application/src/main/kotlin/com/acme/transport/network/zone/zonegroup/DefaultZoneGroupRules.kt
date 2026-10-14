package com.acme.transport.network.zone.zonegroup

/** A rule a default zone group must pass. Check returns its problems, or none. */
interface DefaultZoneGroupRule {
    fun check(item: DefaultZoneGroup): MutableList<String>
}

data class DefaultZoneGroupCodePrefixRule(var prefix: String) : DefaultZoneGroupRule {
    override fun check(item: DefaultZoneGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultZoneGroupRequiredShiftCodeRule(var message: String) : DefaultZoneGroupRule {
    override fun check(item: DefaultZoneGroup): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultZoneGroupRequiredTitleRule(var message: String) : DefaultZoneGroupRule {
    override fun check(item: DefaultZoneGroup): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default zone group. */
data class DefaultZoneGroupValidator(var rules: MutableList<DefaultZoneGroupRule>) {
    fun validate(item: DefaultZoneGroup): MutableList<String> {
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

    fun requireValid(item: DefaultZoneGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultZoneGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultZoneGroupValidator(): DefaultZoneGroupValidator {
    return DefaultZoneGroupValidator(
        mutableListOf<DefaultZoneGroupRule>(
            DefaultZoneGroupCodePrefixRule("ACM"),
            DefaultZoneGroupRequiredShiftCodeRule("default zone group needs a value"),
            DefaultZoneGroupRequiredTitleRule("default zone group needs a value"),
        ),
    )
}
