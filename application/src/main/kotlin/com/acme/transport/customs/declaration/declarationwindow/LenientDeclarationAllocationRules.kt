package com.acme.transport.customs.declaration.declarationwindow

/** A rule a lenient declaration allocation must pass. Check returns its problems, or none. */
interface LenientDeclarationAllocationRule {
    fun check(item: LenientDeclarationAllocation): MutableList<String>
}

data class LenientDeclarationAllocationStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : LenientDeclarationAllocationRule {
    override fun check(item: LenientDeclarationAllocation): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientDeclarationAllocationRequiredNameRule(
    var message: String,
) : LenientDeclarationAllocationRule {
    override fun check(item: LenientDeclarationAllocation): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDeclarationAllocationMaximumRangeRule(
    var min: Double,
    var max: Double,
) : LenientDeclarationAllocationRule {
    override fun check(item: LenientDeclarationAllocation): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lenient declaration allocation. */
data class LenientDeclarationAllocationValidator(var rules: MutableList<LenientDeclarationAllocationRule>) {
    fun validate(item: LenientDeclarationAllocation): MutableList<String> {
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

    fun requireValid(item: LenientDeclarationAllocation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDeclarationAllocation>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDeclarationAllocationValidator(): LenientDeclarationAllocationValidator {
    return LenientDeclarationAllocationValidator(
        mutableListOf<LenientDeclarationAllocationRule>(
            LenientDeclarationAllocationStartsAtRangeRule(255, 86400),
            LenientDeclarationAllocationRequiredNameRule("lenient declaration allocation needs a value"),
            LenientDeclarationAllocationMaximumRangeRule(9.2, 3380.0),
        ),
    )
}
