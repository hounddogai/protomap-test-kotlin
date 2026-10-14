package com.acme.transport.linehaul.capacity.target

import com.acme.transport.kit.text.normalize

/** A rule a capacity target must pass. Check returns its problems, or none. */
interface CapacityTargetRule {
    fun check(item: CapacityTarget): MutableList<String>
}

data class CapacityTargetSegmentLengthRule(var maxLength: Int) : CapacityTargetRule {
    override fun check(item: CapacityTarget): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityTargetCodePrefixRule(var prefix: String) : CapacityTargetRule {
    override fun check(item: CapacityTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CapacityTargetRequiredLabelRule(var message: String) : CapacityTargetRule {
    override fun check(item: CapacityTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityTargetRequiredSegmentRule(var message: String) : CapacityTargetRule {
    override fun check(item: CapacityTarget): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CapacityTargetAllowedReviewerRule(var allowed: MutableList<String>) : CapacityTargetRule {
    override fun check(item: CapacityTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.reviewer))) {
            return mutableListOf<String>("reviewer ${item.reviewer} is blocked")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity target. */
data class CapacityTargetValidator(var rules: MutableList<CapacityTargetRule>) {
    fun validate(item: CapacityTarget): MutableList<String> {
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

    fun requireValid(item: CapacityTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityTargetValidator(): CapacityTargetValidator {
    return CapacityTargetValidator(
        mutableListOf<CapacityTargetRule>(
            CapacityTargetSegmentLengthRule(32),
            CapacityTargetCodePrefixRule("Q"),
            CapacityTargetRequiredLabelRule("capacity target needs an entry"),
            CapacityTargetRequiredSegmentRule("capacity target needs a value"),
            CapacityTargetAllowedReviewerRule(mutableListOf<String>("seasonal", "legacy")),
        ),
    )
}
