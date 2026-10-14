package com.acme.transport.linehaul.relay.window

import com.acme.transport.kit.text.normalize

/** A rule a primary relay window must pass. Check returns its problems, or none. */
interface PrimaryRelayWindowRule {
    fun check(item: PrimaryRelayWindow): MutableList<String>
}

data class PrimaryRelayWindowAllowedNameRule(var allowed: MutableList<String>) : PrimaryRelayWindowRule {
    override fun check(item: PrimaryRelayWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRelayWindowVersionRangeRule(var min: Int, var max: Int) : PrimaryRelayWindowRule {
    override fun check(item: PrimaryRelayWindow): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRelayWindowRequiredCodeRule(var message: String) : PrimaryRelayWindowRule {
    override fun check(item: PrimaryRelayWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary relay window. */
data class PrimaryRelayWindowValidator(var rules: MutableList<PrimaryRelayWindowRule>) {
    fun validate(item: PrimaryRelayWindow): MutableList<String> {
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

    fun requireValid(item: PrimaryRelayWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRelayWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRelayWindowValidator(): PrimaryRelayWindowValidator {
    return PrimaryRelayWindowValidator(
        mutableListOf<PrimaryRelayWindowRule>(
            PrimaryRelayWindowAllowedNameRule(mutableListOf<String>("scheduled")),
            PrimaryRelayWindowVersionRangeRule(32, 32),
            PrimaryRelayWindowRequiredCodeRule("primary relay window needs this field"),
        ),
    )
}
