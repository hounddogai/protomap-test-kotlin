package com.acme.transport.network.node.metric

/** A rule a node audit must pass. Check returns its problems, or none. */
interface NodeAuditRule {
    fun check(item: NodeAudit): MutableList<String>
}

data class NodeAuditLoadKgRangeRule(var min: Double, var max: Double) : NodeAuditRule {
    override fun check(item: NodeAudit): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

data class NodeAuditRequiredOwnerIdRule(var message: String) : NodeAuditRule {
    override fun check(item: NodeAudit): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeAuditWeightRangeRule(var min: Double, var max: Double) : NodeAuditRule {
    override fun check(item: NodeAudit): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node audit. */
data class NodeAuditValidator(var rules: MutableList<NodeAuditRule>) {
    fun validate(item: NodeAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeAuditValidator(): NodeAuditValidator {
    return NodeAuditValidator(
        mutableListOf<NodeAuditRule>(
            NodeAuditLoadKgRangeRule(9.1, 3130.7),
            NodeAuditRequiredOwnerIdRule("node audit needs this field"),
            NodeAuditWeightRangeRule(7.6, 151.0),
        ),
    )
}
