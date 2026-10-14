package com.acme.transport.customs.clearance.cycle

/** A rule a clearance cycle must pass. Check returns its problems, or none. */
interface ClearanceCycleRule {
    fun check(item: ClearanceCycle): MutableList<String>
}

data class ClearanceCycleCodeLengthRule(var maxLength: Int) : ClearanceCycleRule {
    override fun check(item: ClearanceCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ClearanceCycleRequiredCodeRule(var message: String) : ClearanceCycleRule {
    override fun check(item: ClearanceCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ClearanceCycleRequiredLaneRule(var message: String) : ClearanceCycleRule {
    override fun check(item: ClearanceCycle): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a clearance cycle. */
data class ClearanceCycleValidator(var rules: MutableList<ClearanceCycleRule>) {
    fun validate(item: ClearanceCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ClearanceCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ClearanceCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultClearanceCycleValidator(): ClearanceCycleValidator {
    return ClearanceCycleValidator(
        mutableListOf<ClearanceCycleRule>(
            ClearanceCycleCodeLengthRule(32),
            ClearanceCycleRequiredCodeRule("clearance cycle needs an entry"),
            ClearanceCycleRequiredLaneRule("clearance cycle needs an entry"),
        ),
    )
}
