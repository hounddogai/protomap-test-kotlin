package com.acme.transport.linehaul.relay.review

/** A rule a strict relay window must pass. Check returns its problems, or none. */
interface StrictRelayWindowRule {
    fun check(item: StrictRelayWindow): MutableList<String>
}

data class StrictRelayWindowCostRangeRule(var min: Double, var max: Double) : StrictRelayWindowRule {
    override fun check(item: StrictRelayWindow): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class StrictRelayWindowCodePrefixRule(var prefix: String) : StrictRelayWindowRule {
    override fun check(item: StrictRelayWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictRelayWindowRequiredLabelRule(var message: String) : StrictRelayWindowRule {
    override fun check(item: StrictRelayWindow): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRelayWindowRequiredCodeRule(var message: String) : StrictRelayWindowRule {
    override fun check(item: StrictRelayWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictRelayWindowPriorityRangeRule(var min: Int, var max: Int) : StrictRelayWindowRule {
    override fun check(item: StrictRelayWindow): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict relay window. */
data class StrictRelayWindowValidator(var rules: MutableList<StrictRelayWindowRule>) {
    fun validate(item: StrictRelayWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictRelayWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictRelayWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictRelayWindowValidator(): StrictRelayWindowValidator {
    return StrictRelayWindowValidator(
        mutableListOf<StrictRelayWindowRule>(
            StrictRelayWindowCostRangeRule(4.1, 3636.7),
            StrictRelayWindowCodePrefixRule("REF"),
            StrictRelayWindowRequiredLabelRule("strict relay window needs a value"),
            StrictRelayWindowRequiredCodeRule("strict relay window needs this field"),
            StrictRelayWindowPriorityRangeRule(255, 1000),
        ),
    )
}
