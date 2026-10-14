package com.acme.transport.emissions.evcharge.allocation

/** A rule a ev charge allocation must pass. Check returns its problems, or none. */
interface EvChargeAllocationRule {
    fun check(item: EvChargeAllocation): MutableList<String>
}

data class EvChargeAllocationRetriesRangeRule(var min: Int, var max: Int) : EvChargeAllocationRule {
    override fun check(item: EvChargeAllocation): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class EvChargeAllocationTimezoneLengthRule(var maxLength: Int) : EvChargeAllocationRule {
    override fun check(item: EvChargeAllocation): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeAllocationCodePrefixRule(var prefix: String) : EvChargeAllocationRule {
    override fun check(item: EvChargeAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeAllocationCodeLengthRule(var maxLength: Int) : EvChargeAllocationRule {
    override fun check(item: EvChargeAllocation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeAllocationPinnedGuardRule(var limit: Int) : EvChargeAllocationRule {
    override fun check(item: EvChargeAllocation): MutableList<String> {
        if (item.pinned && item.retries > limit) {
            return mutableListOf<String>("retries ${item.retries} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeAllocationRequiredCodeRule(var message: String) : EvChargeAllocationRule {
    override fun check(item: EvChargeAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge allocation. */
data class EvChargeAllocationValidator(var rules: MutableList<EvChargeAllocationRule>) {
    fun validate(item: EvChargeAllocation): MutableList<String> {
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

    fun requireValid(item: EvChargeAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeAllocationValidator(): EvChargeAllocationValidator {
    return EvChargeAllocationValidator(
        mutableListOf<EvChargeAllocationRule>(
            EvChargeAllocationRetriesRangeRule(32, 255),
            EvChargeAllocationTimezoneLengthRule(3600),
            EvChargeAllocationCodePrefixRule("ACM"),
            EvChargeAllocationCodeLengthRule(120),
            EvChargeAllocationPinnedGuardRule(80),
            EvChargeAllocationRequiredCodeRule("ev charge allocation needs a value"),
        ),
    )
}
