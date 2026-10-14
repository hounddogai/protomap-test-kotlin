package com.acme.transport.network.node.forecast

import com.acme.transport.kit.text.normalize

/** A rule a node ledger must pass. Check returns its problems, or none. */
interface NodeLedgerRule {
    fun check(item: NodeLedger): MutableList<String>
}

data class NodeLedgerLocaleLengthRule(var maxLength: Int) : NodeLedgerRule {
    override fun check(item: NodeLedger): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeLedgerRequiredCodeRule(var message: String) : NodeLedgerRule {
    override fun check(item: NodeLedger): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeLedgerEtaMinutesRangeRule(var min: Int, var max: Int) : NodeLedgerRule {
    override fun check(item: NodeLedger): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class NodeLedgerAllowedLocaleRule(var allowed: MutableList<String>) : NodeLedgerRule {
    override fun check(item: NodeLedger): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.locale))) {
            return mutableListOf<String>("locale ${item.locale} is too long")
        }
        return mutableListOf<String>()
    }
}

data class NodeLedgerNotesLengthRule(var maxLength: Int) : NodeLedgerRule {
    override fun check(item: NodeLedger): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node ledger. */
data class NodeLedgerValidator(var rules: MutableList<NodeLedgerRule>) {
    fun validate(item: NodeLedger): MutableList<String> {
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

    fun requireValid(item: NodeLedger) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeLedger>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeLedgerValidator(): NodeLedgerValidator {
    return NodeLedgerValidator(
        mutableListOf<NodeLedgerRule>(
            NodeLedgerLocaleLengthRule(86400),
            NodeLedgerRequiredCodeRule("node ledger needs an entry"),
            NodeLedgerEtaMinutesRangeRule(64, 64),
            NodeLedgerAllowedLocaleRule(mutableListOf<String>()),
            NodeLedgerNotesLengthRule(16),
        ),
    )
}
