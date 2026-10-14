package com.acme.transport.linehaul.relay.segment

/** A rule a default relay baseline must pass. Check returns its problems, or none. */
interface DefaultRelayBaselineRule {
    fun check(item: DefaultRelayBaseline): MutableList<String>
}

data class DefaultRelayBaselineCodeLengthRule(var maxLength: Int) : DefaultRelayBaselineRule {
    override fun check(item: DefaultRelayBaseline): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayBaselineRequiredDockRule(var message: String) : DefaultRelayBaselineRule {
    override fun check(item: DefaultRelayBaseline): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayBaselineRequiredCodeRule(var message: String) : DefaultRelayBaselineRule {
    override fun check(item: DefaultRelayBaseline): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default relay baseline. */
data class DefaultRelayBaselineValidator(var rules: MutableList<DefaultRelayBaselineRule>) {
    fun validate(item: DefaultRelayBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultRelayBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRelayBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRelayBaselineValidator(): DefaultRelayBaselineValidator {
    return DefaultRelayBaselineValidator(
        mutableListOf<DefaultRelayBaselineRule>(
            DefaultRelayBaselineCodeLengthRule(64),
            DefaultRelayBaselineRequiredDockRule("default relay baseline needs a value"),
            DefaultRelayBaselineRequiredCodeRule("default relay baseline needs this field"),
        ),
    )
}
