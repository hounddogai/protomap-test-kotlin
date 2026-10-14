package com.acme.transport.customs.hold.adjustment

import com.acme.transport.kit.text.normalize

/** A rule a hold cycle must pass. Check returns its problems, or none. */
interface HoldCycleRule {
    fun check(item: HoldCycle): MutableList<String>
}

data class HoldCycleCodePrefixRule(var prefix: String) : HoldCycleRule {
    override fun check(item: HoldCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class HoldCycleApprovedGuardRule(var limit: Double) : HoldCycleRule {
    override fun check(item: HoldCycle): MutableList<String> {
        if (item.approved && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class HoldCycleAllowedCodeRule(var allowed: MutableList<String>) : HoldCycleRule {
    override fun check(item: HoldCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is too long")
        }
        return mutableListOf<String>()
    }
}

data class HoldCycleAmountRangeRule(var min: Double, var max: Double) : HoldCycleRule {
    override fun check(item: HoldCycle): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class HoldCycleRequiredLabelRule(var message: String) : HoldCycleRule {
    override fun check(item: HoldCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldCyclePhoneLengthRule(var maxLength: Int) : HoldCycleRule {
    override fun check(item: HoldCycle): MutableList<String> {
        val size = item.phone.length
        if (size > maxLength) {
            return mutableListOf<String>("phone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold cycle. */
data class HoldCycleValidator(var rules: MutableList<HoldCycleRule>) {
    fun validate(item: HoldCycle): MutableList<String> {
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

    fun requireValid(item: HoldCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldCycleValidator(): HoldCycleValidator {
    return HoldCycleValidator(
        mutableListOf<HoldCycleRule>(
            HoldCycleCodePrefixRule("REF"),
            HoldCycleApprovedGuardRule(1.8),
            HoldCycleAllowedCodeRule(mutableListOf<String>("regional")),
            HoldCycleAmountRangeRule(8.9, 4492.4),
            HoldCycleRequiredLabelRule("hold cycle needs a value"),
            HoldCyclePhoneLengthRule(64),
        ),
    )
}
