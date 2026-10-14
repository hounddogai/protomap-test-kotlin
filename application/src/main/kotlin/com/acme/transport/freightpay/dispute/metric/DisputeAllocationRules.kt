package com.acme.transport.freightpay.dispute.metric

/** A rule a dispute allocation must pass. Check returns its problems, or none. */
interface DisputeAllocationRule {
    fun check(item: DisputeAllocation): MutableList<String>
}

data class DisputeAllocationWindowDaysRangeRule(var min: Int, var max: Int) : DisputeAllocationRule {
    override fun check(item: DisputeAllocation): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DisputeAllocationNameLengthRule(var maxLength: Int) : DisputeAllocationRule {
    override fun check(item: DisputeAllocation): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DisputeAllocationRequiredNameRule(var message: String) : DisputeAllocationRule {
    override fun check(item: DisputeAllocation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DisputeAllocationCodePrefixRule(var prefix: String) : DisputeAllocationRule {
    override fun check(item: DisputeAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a dispute allocation. */
data class DisputeAllocationValidator(var rules: MutableList<DisputeAllocationRule>) {
    fun validate(item: DisputeAllocation): MutableList<String> {
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

    fun requireValid(item: DisputeAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DisputeAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultDisputeAllocationValidator(): DisputeAllocationValidator {
    return DisputeAllocationValidator(
        mutableListOf<DisputeAllocationRule>(
            DisputeAllocationWindowDaysRangeRule(16, 86400),
            DisputeAllocationNameLengthRule(255),
            DisputeAllocationRequiredNameRule("dispute allocation needs an entry"),
            DisputeAllocationCodePrefixRule("REF"),
        ),
    )
}
