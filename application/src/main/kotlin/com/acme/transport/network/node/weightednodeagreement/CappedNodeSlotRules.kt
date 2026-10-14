package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.text.normalize

/** A rule a capped node slot must pass. Check returns its problems, or none. */
interface CappedNodeSlotRule {
    fun check(item: CappedNodeSlot): MutableList<String>
}

data class CappedNodeSlotLabelLengthRule(var maxLength: Int) : CappedNodeSlotRule {
    override fun check(item: CappedNodeSlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedNodeSlotGraceDaysRangeRule(var min: Int, var max: Int) : CappedNodeSlotRule {
    override fun check(item: CappedNodeSlot): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedNodeSlotAllowedNameRule(var allowed: MutableList<String>) : CappedNodeSlotRule {
    override fun check(item: CappedNodeSlot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

data class CappedNodeSlotRequiredNameRule(var message: String) : CappedNodeSlotRule {
    override fun check(item: CappedNodeSlot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedNodeSlotCodePrefixRule(var prefix: String) : CappedNodeSlotRule {
    override fun check(item: CappedNodeSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped node slot. */
data class CappedNodeSlotValidator(var rules: MutableList<CappedNodeSlotRule>) {
    fun validate(item: CappedNodeSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedNodeSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedNodeSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedNodeSlotValidator(): CappedNodeSlotValidator {
    return CappedNodeSlotValidator(
        mutableListOf<CappedNodeSlotRule>(
            CappedNodeSlotLabelLengthRule(3600),
            CappedNodeSlotGraceDaysRangeRule(120, 1000),
            CappedNodeSlotAllowedNameRule(mutableListOf<String>("pending", "scheduled")),
            CappedNodeSlotRequiredNameRule("capped node slot needs this field"),
            CappedNodeSlotCodePrefixRule("Q"),
        ),
    )
}
