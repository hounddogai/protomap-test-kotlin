package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.text.normalize

/** A rule a trailer yard policy must pass. Check returns its problems, or none. */
interface TrailerYardPolicyRule {
    fun check(item: TrailerYardPolicy): MutableList<String>
}

data class TrailerYardPolicyLoadKgRangeRule(var min: Double, var max: Double) : TrailerYardPolicyRule {
    override fun check(item: TrailerYardPolicy): MutableList<String> {
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

data class TrailerYardPolicyCodeLengthRule(var maxLength: Int) : TrailerYardPolicyRule {
    override fun check(item: TrailerYardPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardPolicyRequiredNotesRule(var message: String) : TrailerYardPolicyRule {
    override fun check(item: TrailerYardPolicy): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardPolicyAllowedShiftCodeRule(var allowed: MutableList<String>) : TrailerYardPolicyRule {
    override fun check(item: TrailerYardPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardPolicyNameLengthRule(var maxLength: Int) : TrailerYardPolicyRule {
    override fun check(item: TrailerYardPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerYardPolicyApprovedGuardRule(var limit: Double) : TrailerYardPolicyRule {
    override fun check(item: TrailerYardPolicy): MutableList<String> {
        if (item.approved && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer yard policy. */
data class TrailerYardPolicyValidator(var rules: MutableList<TrailerYardPolicyRule>) {
    fun validate(item: TrailerYardPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TrailerYardPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerYardPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerYardPolicyValidator(): TrailerYardPolicyValidator {
    return TrailerYardPolicyValidator(
        mutableListOf<TrailerYardPolicyRule>(
            TrailerYardPolicyLoadKgRangeRule(9.4, 127.2),
            TrailerYardPolicyCodeLengthRule(3600),
            TrailerYardPolicyRequiredNotesRule("trailer yard policy needs this field"),
            TrailerYardPolicyAllowedShiftCodeRule(mutableListOf<String>("seasonal", "default")),
            TrailerYardPolicyNameLengthRule(64),
            TrailerYardPolicyApprovedGuardRule(9.5),
        ),
    )
}
