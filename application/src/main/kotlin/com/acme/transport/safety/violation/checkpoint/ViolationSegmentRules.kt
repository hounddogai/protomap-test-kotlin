package com.acme.transport.safety.violation.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a violation segment must pass. Check returns its problems, or none. */
interface ViolationSegmentRule {
    fun check(item: ViolationSegment): MutableList<String>
}

data class ViolationSegmentRequiredCodeRule(var message: String) : ViolationSegmentRule {
    override fun check(item: ViolationSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationSegmentAllowedCodeRule(var allowed: MutableList<String>) : ViolationSegmentRule {
    override fun check(item: ViolationSegment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class ViolationSegmentRequiredNameRule(var message: String) : ViolationSegmentRule {
    override fun check(item: ViolationSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ViolationSegmentCodePrefixRule(var prefix: String) : ViolationSegmentRule {
    override fun check(item: ViolationSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation segment. */
data class ViolationSegmentValidator(var rules: MutableList<ViolationSegmentRule>) {
    fun validate(item: ViolationSegment): MutableList<String> {
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

    fun requireValid(item: ViolationSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationSegmentValidator(): ViolationSegmentValidator {
    return ViolationSegmentValidator(
        mutableListOf<ViolationSegmentRule>(
            ViolationSegmentRequiredCodeRule("violation segment needs this field"),
            ViolationSegmentAllowedCodeRule(mutableListOf<String>("strict", "global", "pending")),
            ViolationSegmentRequiredNameRule("violation segment needs an entry"),
            ViolationSegmentCodePrefixRule("OPS"),
        ),
    )
}
