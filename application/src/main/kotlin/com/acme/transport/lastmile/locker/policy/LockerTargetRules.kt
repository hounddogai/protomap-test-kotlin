package com.acme.transport.lastmile.locker.policy

/** A rule a locker target must pass. Check returns its problems, or none. */
interface LockerTargetRule {
    fun check(item: LockerTarget): MutableList<String>
}

data class LockerTargetRequiredReviewerRule(var message: String) : LockerTargetRule {
    override fun check(item: LockerTarget): MutableList<String> {
        if (item.reviewer.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerTargetReviewerLengthRule(var maxLength: Int) : LockerTargetRule {
    override fun check(item: LockerTarget): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LockerTargetRequiredCodeRule(var message: String) : LockerTargetRule {
    override fun check(item: LockerTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a locker target. */
data class LockerTargetValidator(var rules: MutableList<LockerTargetRule>) {
    fun validate(item: LockerTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerTargetValidator(): LockerTargetValidator {
    return LockerTargetValidator(
        mutableListOf<LockerTargetRule>(
            LockerTargetRequiredReviewerRule("locker target needs a value"),
            LockerTargetReviewerLengthRule(255),
            LockerTargetRequiredCodeRule("locker target needs this field"),
        ),
    )
}
