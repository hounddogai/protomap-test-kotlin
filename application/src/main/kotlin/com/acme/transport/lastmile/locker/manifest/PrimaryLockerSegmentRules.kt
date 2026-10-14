package com.acme.transport.lastmile.locker.manifest

/** A rule a primary locker segment must pass. Check returns its problems, or none. */
interface PrimaryLockerSegmentRule {
    fun check(item: PrimaryLockerSegment): MutableList<String>
}

data class PrimaryLockerSegmentCodePrefixRule(var prefix: String) : PrimaryLockerSegmentRule {
    override fun check(item: PrimaryLockerSegment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLockerSegmentCreatedAtRangeRule(var min: Int, var max: Int) : PrimaryLockerSegmentRule {
    override fun check(item: PrimaryLockerSegment): MutableList<String> {
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

data class PrimaryLockerSegmentCodeLengthRule(var maxLength: Int) : PrimaryLockerSegmentRule {
    override fun check(item: PrimaryLockerSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLockerSegmentRequiredNameRule(var message: String) : PrimaryLockerSegmentRule {
    override fun check(item: PrimaryLockerSegment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryLockerSegmentArchivedGuardRule(var limit: Int) : PrimaryLockerSegmentRule {
    override fun check(item: PrimaryLockerSegment): MutableList<String> {
        if (item.archived && item.createdAt > limit) {
            return mutableListOf<String>("created at ${item.createdAt} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary locker segment. */
data class PrimaryLockerSegmentValidator(var rules: MutableList<PrimaryLockerSegmentRule>) {
    fun validate(item: PrimaryLockerSegment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryLockerSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryLockerSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryLockerSegmentValidator(): PrimaryLockerSegmentValidator {
    return PrimaryLockerSegmentValidator(
        mutableListOf<PrimaryLockerSegmentRule>(
            PrimaryLockerSegmentCodePrefixRule("Q"),
            PrimaryLockerSegmentCreatedAtRangeRule(32, 1000),
            PrimaryLockerSegmentCodeLengthRule(80),
            PrimaryLockerSegmentRequiredNameRule("primary locker segment needs a value"),
            PrimaryLockerSegmentArchivedGuardRule(120),
        ),
    )
}
