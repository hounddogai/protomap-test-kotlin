package com.acme.transport.freightpay.accessorial.entry

/** A rule a rolling accessorial allocation must pass. Check returns its problems, or none. */
interface RollingAccessorialAllocationRule {
    fun check(item: RollingAccessorialAllocation): MutableList<String>
}

data class RollingAccessorialAllocationContactEmailLengthRule(
    var maxLength: Int,
) : RollingAccessorialAllocationRule {
    override fun check(item: RollingAccessorialAllocation): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingAccessorialAllocationRequiredLabelRule(
    var message: String,
) : RollingAccessorialAllocationRule {
    override fun check(item: RollingAccessorialAllocation): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingAccessorialAllocationCodePrefixRule(var prefix: String) : RollingAccessorialAllocationRule {
    override fun check(item: RollingAccessorialAllocation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingAccessorialAllocationTrailerIdLengthRule(
    var maxLength: Int,
) : RollingAccessorialAllocationRule {
    override fun check(item: RollingAccessorialAllocation): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling accessorial allocation. */
data class RollingAccessorialAllocationValidator(var rules: MutableList<RollingAccessorialAllocationRule>) {
    fun validate(item: RollingAccessorialAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingAccessorialAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingAccessorialAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingAccessorialAllocationValidator(): RollingAccessorialAllocationValidator {
    return RollingAccessorialAllocationValidator(
        mutableListOf<RollingAccessorialAllocationRule>(
            RollingAccessorialAllocationContactEmailLengthRule(32),
            RollingAccessorialAllocationRequiredLabelRule("rolling accessorial allocation needs this field"),
            RollingAccessorialAllocationCodePrefixRule("Q"),
            RollingAccessorialAllocationTrailerIdLengthRule(3600),
        ),
    )
}
