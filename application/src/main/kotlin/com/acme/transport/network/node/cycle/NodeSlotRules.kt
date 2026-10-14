package com.acme.transport.network.node.cycle

/** A rule a node slot must pass. Check returns its problems, or none. */
interface NodeSlotRule {
    fun check(item: NodeSlot): MutableList<String>
}

data class NodeSlotRequiredLabelRule(var message: String) : NodeSlotRule {
    override fun check(item: NodeSlot): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeSlotCodeLengthRule(var maxLength: Int) : NodeSlotRule {
    override fun check(item: NodeSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeSlotAmountRangeRule(var min: Double, var max: Double) : NodeSlotRule {
    override fun check(item: NodeSlot): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeSlotRequiredCodeRule(var message: String) : NodeSlotRule {
    override fun check(item: NodeSlot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeSlotLabelLengthRule(var maxLength: Int) : NodeSlotRule {
    override fun check(item: NodeSlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeSlotDiscountRangeRule(var min: Double, var max: Double) : NodeSlotRule {
    override fun check(item: NodeSlot): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a node slot. */
data class NodeSlotValidator(var rules: MutableList<NodeSlotRule>) {
    fun validate(item: NodeSlot): MutableList<String> {
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

    fun requireValid(item: NodeSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeSlotValidator(): NodeSlotValidator {
    return NodeSlotValidator(
        mutableListOf<NodeSlotRule>(
            NodeSlotRequiredLabelRule("node slot needs an entry"),
            NodeSlotCodeLengthRule(120),
            NodeSlotAmountRangeRule(3.8, 1151.7),
            NodeSlotRequiredCodeRule("node slot needs a value"),
            NodeSlotLabelLengthRule(80),
            NodeSlotDiscountRangeRule(5.9, 3905.7),
        ),
    )
}
