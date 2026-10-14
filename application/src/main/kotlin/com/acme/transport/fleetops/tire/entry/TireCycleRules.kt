package com.acme.transport.fleetops.tire.entry

/** A rule a tire cycle must pass. Check returns its problems, or none. */
interface TireCycleRule {
    fun check(item: TireCycle): MutableList<String>
}

data class TireCycleRequiredDescriptionRule(var message: String) : TireCycleRule {
    override fun check(item: TireCycle): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireCycleCodeLengthRule(var maxLength: Int) : TireCycleRule {
    override fun check(item: TireCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireCycleActiveGuardRule(var limit: Int) : TireCycleRule {
    override fun check(item: TireCycle): MutableList<String> {
        if (item.active && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire cycle. */
data class TireCycleValidator(var rules: MutableList<TireCycleRule>) {
    fun validate(item: TireCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultTireCycleValidator(): TireCycleValidator {
    return TireCycleValidator(
        mutableListOf<TireCycleRule>(
            TireCycleRequiredDescriptionRule("tire cycle needs this field"),
            TireCycleCodeLengthRule(32),
            TireCycleActiveGuardRule(120),
        ),
    )
}
