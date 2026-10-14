package com.acme.transport.linehaul.relay.claim

import com.acme.transport.kit.text.normalize

/** A rule a tiered relay assignment must pass. Check returns its problems, or none. */
interface TieredRelayAssignmentRule {
    fun check(item: TieredRelayAssignment): MutableList<String>
}

data class TieredRelayAssignmentRequiredCodeRule(var message: String) : TieredRelayAssignmentRule {
    override fun check(item: TieredRelayAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRelayAssignmentAllowedCodeRule(var allowed: MutableList<String>) : TieredRelayAssignmentRule {
    override fun check(item: TieredRelayAssignment): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class TieredRelayAssignmentDiscountRangeRule(
    var min: Double,
    var max: Double,
) : TieredRelayAssignmentRule {
    override fun check(item: TieredRelayAssignment): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class TieredRelayAssignmentRequiredChecksumRule(var message: String) : TieredRelayAssignmentRule {
    override fun check(item: TieredRelayAssignment): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRelayAssignmentCodePrefixRule(var prefix: String) : TieredRelayAssignmentRule {
    override fun check(item: TieredRelayAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredRelayAssignmentNameLengthRule(var maxLength: Int) : TieredRelayAssignmentRule {
    override fun check(item: TieredRelayAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered relay assignment. */
data class TieredRelayAssignmentValidator(var rules: MutableList<TieredRelayAssignmentRule>) {
    fun validate(item: TieredRelayAssignment): MutableList<String> {
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

    fun requireValid(item: TieredRelayAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRelayAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRelayAssignmentValidator(): TieredRelayAssignmentValidator {
    return TieredRelayAssignmentValidator(
        mutableListOf<TieredRelayAssignmentRule>(
            TieredRelayAssignmentRequiredCodeRule("tiered relay assignment needs this field"),
            TieredRelayAssignmentAllowedCodeRule(mutableListOf<String>("adaptive", "primary")),
            TieredRelayAssignmentDiscountRangeRule(4.1, 1619.4),
            TieredRelayAssignmentRequiredChecksumRule("tiered relay assignment needs a value"),
            TieredRelayAssignmentCodePrefixRule("OPS"),
            TieredRelayAssignmentNameLengthRule(80),
        ),
    )
}
