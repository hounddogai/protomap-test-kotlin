package com.acme.transport.linehaul.relay.window

import com.acme.transport.kit.text.normalize

/** A rule a rolling relay entry must pass. Check returns its problems, or none. */
interface RollingRelayEntryRule {
    fun check(item: RollingRelayEntry): MutableList<String>
}

data class RollingRelayEntryAttemptsRangeRule(var min: Int, var max: Int) : RollingRelayEntryRule {
    override fun check(item: RollingRelayEntry): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

data class RollingRelayEntryRequiredNameRule(var message: String) : RollingRelayEntryRule {
    override fun check(item: RollingRelayEntry): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRelayEntryAllowedSourceRule(var allowed: MutableList<String>) : RollingRelayEntryRule {
    override fun check(item: RollingRelayEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RollingRelayEntryRequiredCodeRule(var message: String) : RollingRelayEntryRule {
    override fun check(item: RollingRelayEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingRelayEntryRequiredCarrierRule(var message: String) : RollingRelayEntryRule {
    override fun check(item: RollingRelayEntry): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling relay entry. */
data class RollingRelayEntryValidator(var rules: MutableList<RollingRelayEntryRule>) {
    fun validate(item: RollingRelayEntry): MutableList<String> {
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

    fun requireValid(item: RollingRelayEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingRelayEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingRelayEntryValidator(): RollingRelayEntryValidator {
    return RollingRelayEntryValidator(
        mutableListOf<RollingRelayEntryRule>(
            RollingRelayEntryAttemptsRangeRule(32, 32),
            RollingRelayEntryRequiredNameRule("rolling relay entry needs a value"),
            RollingRelayEntryAllowedSourceRule(mutableListOf<String>("default", "global", "rolling")),
            RollingRelayEntryRequiredCodeRule("rolling relay entry needs this field"),
            RollingRelayEntryRequiredCarrierRule("rolling relay entry needs a value"),
        ),
    )
}
