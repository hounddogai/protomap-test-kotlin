package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.text.normalize

/** A rule a lenient drop target must pass. Check returns its problems, or none. */
interface LenientDropTargetRule {
    fun check(item: LenientDropTarget): MutableList<String>
}

data class LenientDropTargetCapacityRangeRule(var min: Int, var max: Int) : LenientDropTargetRule {
    override fun check(item: LenientDropTarget): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class LenientDropTargetLimitRangeRule(var min: Int, var max: Int) : LenientDropTargetRule {
    override fun check(item: LenientDropTarget): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class LenientDropTargetAllowedDescriptionRule(var allowed: MutableList<String>) : LenientDropTargetRule {
    override fun check(item: LenientDropTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is stale")
        }
        return mutableListOf<String>()
    }
}

data class LenientDropTargetRequiredTitleRule(var message: String) : LenientDropTargetRule {
    override fun check(item: LenientDropTarget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDropTargetRequiredCodeRule(var message: String) : LenientDropTargetRule {
    override fun check(item: LenientDropTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient drop target. */
data class LenientDropTargetValidator(var rules: MutableList<LenientDropTargetRule>) {
    fun validate(item: LenientDropTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientDropTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDropTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDropTargetValidator(): LenientDropTargetValidator {
    return LenientDropTargetValidator(
        mutableListOf<LenientDropTargetRule>(
            LenientDropTargetCapacityRangeRule(120, 3600),
            LenientDropTargetLimitRangeRule(120, 120),
            LenientDropTargetAllowedDescriptionRule(mutableListOf<String>("adaptive")),
            LenientDropTargetRequiredTitleRule("lenient drop target needs a value"),
            LenientDropTargetRequiredCodeRule("lenient drop target needs an entry"),
        ),
    )
}
