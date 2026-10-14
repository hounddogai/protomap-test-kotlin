package com.acme.transport.network.node.manifest

import com.acme.transport.kit.text.normalize

/** A rule a node manifest must pass. Check returns its problems, or none. */
interface NodeManifestRule {
    fun check(item: NodeManifest): MutableList<String>
}

data class NodeManifestRequiredCodeRule(var message: String) : NodeManifestRule {
    override fun check(item: NodeManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeManifestAllowedOwnerIdRule(var allowed: MutableList<String>) : NodeManifestRule {
    override fun check(item: NodeManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class NodeManifestTitleLengthRule(var maxLength: Int) : NodeManifestRule {
    override fun check(item: NodeManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeManifestLoadKgRangeRule(var min: Double, var max: Double) : NodeManifestRule {
    override fun check(item: NodeManifest): MutableList<String> {
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

/** Runs every rule of a node manifest. */
data class NodeManifestValidator(var rules: MutableList<NodeManifestRule>) {
    fun validate(item: NodeManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeManifestValidator(): NodeManifestValidator {
    return NodeManifestValidator(
        mutableListOf<NodeManifestRule>(
            NodeManifestRequiredCodeRule("node manifest needs a value"),
            NodeManifestAllowedOwnerIdRule(mutableListOf<String>("scheduled", "capped")),
            NodeManifestTitleLengthRule(64),
            NodeManifestLoadKgRangeRule(7.4, 3987.2),
        ),
    )
}
