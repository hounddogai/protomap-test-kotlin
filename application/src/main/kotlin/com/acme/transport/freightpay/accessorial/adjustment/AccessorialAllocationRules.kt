package com.acme.transport.freightpay.accessorial.adjustment

/** A rule a accessorial allocation must pass. Check returns its problems, or none. */
interface AccessorialAllocationRule {
    fun check(item: AccessorialAllocation): MutableList<String>
}

data class AccessorialAllocationDriverIdLengthRule(var maxLength: Int) : AccessorialAllocationRule {
    override fun check(item: AccessorialAllocation): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialAllocationLocaleLengthRule(var maxLength: Int) : AccessorialAllocationRule {
    override fun check(item: AccessorialAllocation): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialAllocationVersionRangeRule(var min: Int, var max: Int) : AccessorialAllocationRule {
    override fun check(item: AccessorialAllocation): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AccessorialAllocationRequiredCodeRule(var message: String) : AccessorialAllocationRule {
    override fun check(item: AccessorialAllocation): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a accessorial allocation. */
data class AccessorialAllocationValidator(var rules: MutableList<AccessorialAllocationRule>) {
    fun validate(item: AccessorialAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AccessorialAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AccessorialAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultAccessorialAllocationValidator(): AccessorialAllocationValidator {
    return AccessorialAllocationValidator(
        mutableListOf<AccessorialAllocationRule>(
            AccessorialAllocationDriverIdLengthRule(120),
            AccessorialAllocationLocaleLengthRule(32),
            AccessorialAllocationVersionRangeRule(16, 255),
            AccessorialAllocationRequiredCodeRule("accessorial allocation needs this field"),
        ),
    )
}
