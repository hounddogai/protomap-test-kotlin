package com.acme.transport.lastmile.locker.target

/** A rule a locker segment must pass. Check returns its problems, or none. */
interface LockerSegmentRule {
    fun check(item: LockerSegment): MutableList<String>
}

data class LockerSegmentCarrierLengthRule(var maxLength: Int) : LockerSegmentRule {
    override fun check(item: LockerSegment): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerSegmentCodeLengthRule(var maxLength: Int) : LockerSegmentRule {
    override fun check(item: LockerSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerSegmentCapacityRangeRule(var min: Int, var max: Int) : LockerSegmentRule {
    override fun check(item: LockerSegment): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerSegmentRequiredCodeRule(var message: String) : LockerSegmentRule {
    override fun check(item: LockerSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker segment. */
data class LockerSegmentValidator(var rules: MutableList<LockerSegmentRule>) {
    fun validate(item: LockerSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerSegmentValidator(): LockerSegmentValidator {
    return LockerSegmentValidator(
        mutableListOf<LockerSegmentRule>(
            LockerSegmentCarrierLengthRule(255),
            LockerSegmentCodeLengthRule(32),
            LockerSegmentCapacityRangeRule(16, 86400),
            LockerSegmentRequiredCodeRule("locker segment needs an entry"),
        ),
    )
}
