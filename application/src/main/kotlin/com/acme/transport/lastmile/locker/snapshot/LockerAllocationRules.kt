package com.acme.transport.lastmile.locker.snapshot

/** A rule a locker allocation must pass. Check returns its problems, or none. */
interface LockerAllocationRule {
    fun check(item: LockerAllocation): MutableList<String>
}

data class LockerAllocationTimezoneLengthRule(var maxLength: Int) : LockerAllocationRule {
    override fun check(item: LockerAllocation): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerAllocationCreatedAtRangeRule(var min: Int, var max: Int) : LockerAllocationRule {
    override fun check(item: LockerAllocation): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class LockerAllocationPriorityRangeRule(var min: Int, var max: Int) : LockerAllocationRule {
    override fun check(item: LockerAllocation): MutableList<String> {
        val value = item.priority
        if (value < min || value > max) {
            return mutableListOf<String>("priority $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker allocation. */
data class LockerAllocationValidator(var rules: MutableList<LockerAllocationRule>) {
    fun validate(item: LockerAllocation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerAllocationValidator(): LockerAllocationValidator {
    return LockerAllocationValidator(
        mutableListOf<LockerAllocationRule>(
            LockerAllocationTimezoneLengthRule(120),
            LockerAllocationCreatedAtRangeRule(120, 255),
            LockerAllocationPriorityRangeRule(255, 86400),
        ),
    )
}
