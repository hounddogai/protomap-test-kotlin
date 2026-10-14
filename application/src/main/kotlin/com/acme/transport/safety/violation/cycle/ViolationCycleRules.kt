package com.acme.transport.safety.violation.cycle

/** A rule a violation cycle must pass. Check returns its problems, or none. */
interface ViolationCycleRule {
    fun check(item: ViolationCycle): MutableList<String>
}

data class ViolationCycleCodeLengthRule(var maxLength: Int) : ViolationCycleRule {
    override fun check(item: ViolationCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationCycleActiveGuardRule(var limit: Int) : ViolationCycleRule {
    override fun check(item: ViolationCycle): MutableList<String> {
        if (item.active && item.windowDays > limit) {
            return mutableListOf<String>("window days ${item.windowDays} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ViolationCycleRequiredNameRule(var message: String) : ViolationCycleRule {
    override fun check(item: ViolationCycle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation cycle. */
data class ViolationCycleValidator(var rules: MutableList<ViolationCycleRule>) {
    fun validate(item: ViolationCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationCycleValidator(): ViolationCycleValidator {
    return ViolationCycleValidator(
        mutableListOf<ViolationCycleRule>(
            ViolationCycleCodeLengthRule(1000),
            ViolationCycleActiveGuardRule(120),
            ViolationCycleRequiredNameRule("violation cycle needs an entry"),
        ),
    )
}
