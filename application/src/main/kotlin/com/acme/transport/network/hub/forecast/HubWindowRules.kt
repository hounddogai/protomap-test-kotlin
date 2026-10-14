package com.acme.transport.network.hub.forecast

import com.acme.transport.kit.text.normalize

/** A rule a hub window must pass. Check returns its problems, or none. */
interface HubWindowRule {
    fun check(item: HubWindow): MutableList<String>
}

data class HubWindowCodePrefixRule(var prefix: String) : HubWindowRule {
    override fun check(item: HubWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HubWindowShiftCodeLengthRule(var maxLength: Int) : HubWindowRule {
    override fun check(item: HubWindow): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubWindowAllowedCodeRule(var allowed: MutableList<String>) : HubWindowRule {
    override fun check(item: HubWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class HubWindowCodeLengthRule(var maxLength: Int) : HubWindowRule {
    override fun check(item: HubWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HubWindowNameLengthRule(var maxLength: Int) : HubWindowRule {
    override fun check(item: HubWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hub window. */
data class HubWindowValidator(var rules: MutableList<HubWindowRule>) {
    fun validate(item: HubWindow): MutableList<String> {
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

    fun requireValid(item: HubWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HubWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultHubWindowValidator(): HubWindowValidator {
    return HubWindowValidator(
        mutableListOf<HubWindowRule>(
            HubWindowCodePrefixRule("Q"),
            HubWindowShiftCodeLengthRule(64),
            HubWindowAllowedCodeRule(mutableListOf<String>("adaptive", "adaptive", "lenient")),
            HubWindowCodeLengthRule(255),
            HubWindowNameLengthRule(16),
        ),
    )
}
