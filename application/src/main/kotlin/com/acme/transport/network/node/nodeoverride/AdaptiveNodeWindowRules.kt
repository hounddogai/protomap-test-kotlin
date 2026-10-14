package com.acme.transport.network.node.nodeoverride

/** A rule a adaptive node window must pass. Check returns its problems, or none. */
interface AdaptiveNodeWindowRule {
    fun check(item: AdaptiveNodeWindow): MutableList<String>
}

data class AdaptiveNodeWindowRequiredRouteCodeRule(var message: String) : AdaptiveNodeWindowRule {
    override fun check(item: AdaptiveNodeWindow): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeWindowLabelLengthRule(var maxLength: Int) : AdaptiveNodeWindowRule {
    override fun check(item: AdaptiveNodeWindow): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveNodeWindowCodePrefixRule(var prefix: String) : AdaptiveNodeWindowRule {
    override fun check(item: AdaptiveNodeWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive node window. */
data class AdaptiveNodeWindowValidator(var rules: MutableList<AdaptiveNodeWindowRule>) {
    fun validate(item: AdaptiveNodeWindow): MutableList<String> {
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

    fun requireValid(item: AdaptiveNodeWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveNodeWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveNodeWindowValidator(): AdaptiveNodeWindowValidator {
    return AdaptiveNodeWindowValidator(
        mutableListOf<AdaptiveNodeWindowRule>(
            AdaptiveNodeWindowRequiredRouteCodeRule("adaptive node window needs an entry"),
            AdaptiveNodeWindowLabelLengthRule(64),
            AdaptiveNodeWindowCodePrefixRule("Q"),
        ),
    )
}
