package com.acme.transport.network.node.bundle

/** A rule a node proposal must pass. Check returns its problems, or none. */
interface NodeProposalRule {
    fun check(item: NodeProposal): MutableList<String>
}

data class NodeProposalCodePrefixRule(var prefix: String) : NodeProposalRule {
    override fun check(item: NodeProposal): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NodeProposalPhoneLengthRule(var maxLength: Int) : NodeProposalRule {
    override fun check(item: NodeProposal): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeProposalRequiredCodeRule(var message: String) : NodeProposalRule {
    override fun check(item: NodeProposal): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NodeProposalCodeLengthRule(var maxLength: Int) : NodeProposalRule {
    override fun check(item: NodeProposal): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NodeProposalRequiredDescriptionRule(var message: String) : NodeProposalRule {
    override fun check(item: NodeProposal): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a node proposal. */
data class NodeProposalValidator(var rules: MutableList<NodeProposalRule>) {
    fun validate(item: NodeProposal): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: NodeProposal) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NodeProposal>): MutableMap<String, MutableList<String>> {
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

fun defaultNodeProposalValidator(): NodeProposalValidator {
    return NodeProposalValidator(
        mutableListOf<NodeProposalRule>(
            NodeProposalCodePrefixRule("X"),
            NodeProposalPhoneLengthRule(16),
            NodeProposalRequiredCodeRule("node proposal needs a value"),
            NodeProposalCodeLengthRule(32),
            NodeProposalRequiredDescriptionRule("node proposal needs a value"),
        ),
    )
}
