package com.acme.transport.customs.hold.checkpoint

import com.acme.transport.kit.text.normalize

/** A rule a capped hold checkpoint must pass. Check returns its problems, or none. */
interface CappedHoldCheckpointRule {
    fun check(item: CappedHoldCheckpoint): MutableList<String>
}

data class CappedHoldCheckpointApprovedGuardRule(var limit: Int) : CappedHoldCheckpointRule {
    override fun check(item: CappedHoldCheckpoint): MutableList<String> {
        if (item.approved && item.durationMinutes > limit) {
            return mutableListOf<String>("duration minutes ${item.durationMinutes} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class CappedHoldCheckpointCodeLengthRule(var maxLength: Int) : CappedHoldCheckpointRule {
    override fun check(item: CappedHoldCheckpoint): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedHoldCheckpointCostRangeRule(var min: Double, var max: Double) : CappedHoldCheckpointRule {
    override fun check(item: CappedHoldCheckpoint): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedHoldCheckpointCodePrefixRule(var prefix: String) : CappedHoldCheckpointRule {
    override fun check(item: CappedHoldCheckpoint): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CappedHoldCheckpointRequiredPhoneRule(var message: String) : CappedHoldCheckpointRule {
    override fun check(item: CappedHoldCheckpoint): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedHoldCheckpointAllowedCodeRule(var allowed: MutableList<String>) : CappedHoldCheckpointRule {
    override fun check(item: CappedHoldCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped hold checkpoint. */
data class CappedHoldCheckpointValidator(var rules: MutableList<CappedHoldCheckpointRule>) {
    fun validate(item: CappedHoldCheckpoint): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedHoldCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedHoldCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedHoldCheckpointValidator(): CappedHoldCheckpointValidator {
    return CappedHoldCheckpointValidator(
        mutableListOf<CappedHoldCheckpointRule>(
            CappedHoldCheckpointApprovedGuardRule(255),
            CappedHoldCheckpointCodeLengthRule(3600),
            CappedHoldCheckpointCostRangeRule(3.2, 1578.4),
            CappedHoldCheckpointCodePrefixRule("X"),
            CappedHoldCheckpointRequiredPhoneRule("capped hold checkpoint needs a value"),
            CappedHoldCheckpointAllowedCodeRule(mutableListOf<String>("default", "tiered")),
        ),
    )
}
