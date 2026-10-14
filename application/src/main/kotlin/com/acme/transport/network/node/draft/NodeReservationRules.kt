package com.acme.transport.network.node.draft

import com.acme.transport.kit.text.normalize

/** A rule a node reservation must pass. Check returns its problems, or none. */
interface NodeReservationRule {
    fun check(item: NodeReservation): MutableList<String>
}

data class NodeReservationCarrierLengthRule(var maxLength: Int) : NodeReservationRule {
    override fun check(item: NodeReservation): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeReservationRequiredCodeRule(var message: String) : NodeReservationRule {
    override fun check(item: NodeReservation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeReservationAllowedCarrierRule(var allowed: MutableList<String>) : NodeReservationRule {
    override fun check(item: NodeReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class NodeReservationLabelLengthRule(var maxLength: Int) : NodeReservationRule {
    override fun check(item: NodeReservation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeReservationRequiredLabelRule(var message: String) : NodeReservationRule {
    override fun check(item: NodeReservation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node reservation. */
data class NodeReservationValidator(var rules: MutableList<NodeReservationRule>) {
    fun validate(item: NodeReservation): MutableList<String> {
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

    fun requireValid(item: NodeReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeReservationValidator(): NodeReservationValidator {
    return NodeReservationValidator(
        mutableListOf<NodeReservationRule>(
            NodeReservationCarrierLengthRule(64),
            NodeReservationRequiredCodeRule("node reservation needs this field"),
            NodeReservationAllowedCarrierRule(mutableListOf<String>("strict", "global", "primary")),
            NodeReservationLabelLengthRule(64),
            NodeReservationRequiredLabelRule("node reservation needs an entry"),
        ),
    )
}
