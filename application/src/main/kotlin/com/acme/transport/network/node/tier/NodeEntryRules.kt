package com.acme.transport.network.node.tier

/** A rule a node entry must pass. Check returns its problems, or none. */
interface NodeEntryRule {
    fun check(item: NodeEntry): MutableList<String>
}

data class NodeEntryCodePrefixRule(var prefix: String) : NodeEntryRule {
    override fun check(item: NodeEntry): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NodeEntryRetriesRangeRule(var min: Int, var max: Int) : NodeEntryRule {
    override fun check(item: NodeEntry): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class NodeEntryCodeLengthRule(var maxLength: Int) : NodeEntryRule {
    override fun check(item: NodeEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeEntryFuelLitersRangeRule(var min: Double, var max: Double) : NodeEntryRule {
    override fun check(item: NodeEntry): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class NodeEntryDiscountRangeRule(var min: Double, var max: Double) : NodeEntryRule {
    override fun check(item: NodeEntry): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeEntryRequiredNameRule(var message: String) : NodeEntryRule {
    override fun check(item: NodeEntry): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node entry. */
data class NodeEntryValidator(var rules: MutableList<NodeEntryRule>) {
    fun validate(item: NodeEntry): MutableList<String> {
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

    fun requireValid(item: NodeEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeEntryValidator(): NodeEntryValidator {
    return NodeEntryValidator(
        mutableListOf<NodeEntryRule>(
            NodeEntryCodePrefixRule("OPS"),
            NodeEntryRetriesRangeRule(32, 86400),
            NodeEntryCodeLengthRule(1000),
            NodeEntryFuelLitersRangeRule(7.0, 4640.9),
            NodeEntryDiscountRangeRule(8.3, 294.4),
            NodeEntryRequiredNameRule("node entry needs an entry"),
        ),
    )
}
