package com.acme.transport.network.node.assignment

import com.acme.transport.kit.text.normalize

/** A rule a pending node mapping must pass. Check returns its problems, or none. */
interface PendingNodeMappingRule {
    fun check(item: PendingNodeMapping): MutableList<String>
}

data class PendingNodeMappingAllowedCategoryRule(var allowed: MutableList<String>) : PendingNodeMappingRule {
    override fun check(item: PendingNodeMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.category))) {
            return mutableListOf<String>("category ${item.category} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeMappingRequiredChannelRule(var message: String) : PendingNodeMappingRule {
    override fun check(item: PendingNodeMapping): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeMappingCategoryLengthRule(var maxLength: Int) : PendingNodeMappingRule {
    override fun check(item: PendingNodeMapping): MutableList<String> {
        val size = item.category.length
        if (size > maxLength) {
            return mutableListOf<String>("category is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingNodeMappingRequiredCodeRule(var message: String) : PendingNodeMappingRule {
    override fun check(item: PendingNodeMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending node mapping. */
data class PendingNodeMappingValidator(var rules: MutableList<PendingNodeMappingRule>) {
    fun validate(item: PendingNodeMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PendingNodeMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingNodeMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingNodeMappingValidator(): PendingNodeMappingValidator {
    return PendingNodeMappingValidator(
        mutableListOf<PendingNodeMappingRule>(
            PendingNodeMappingAllowedCategoryRule(mutableListOf<String>("regional", "lenient")),
            PendingNodeMappingRequiredChannelRule("pending node mapping needs this field"),
            PendingNodeMappingCategoryLengthRule(120),
            PendingNodeMappingRequiredCodeRule("pending node mapping needs this field"),
        ),
    )
}
