package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.text.normalize

/** A rule a default drop cycle must pass. Check returns its problems, or none. */
interface DefaultDropCycleRule {
    fun check(item: DefaultDropCycle): MutableList<String>
}

data class DefaultDropCycleAllowedTrailerIdRule(var allowed: MutableList<String>) : DefaultDropCycleRule {
    override fun check(item: DefaultDropCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is stale")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDropCycleRequiredTrailerIdRule(var message: String) : DefaultDropCycleRule {
    override fun check(item: DefaultDropCycle): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDropCycleRequiredLabelRule(var message: String) : DefaultDropCycleRule {
    override fun check(item: DefaultDropCycle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDropCycleCodePrefixRule(var prefix: String) : DefaultDropCycleRule {
    override fun check(item: DefaultDropCycle): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultDropCycleRequiredShiftCodeRule(var message: String) : DefaultDropCycleRule {
    override fun check(item: DefaultDropCycle): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultDropCycleRequiredCodeRule(var message: String) : DefaultDropCycleRule {
    override fun check(item: DefaultDropCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default drop cycle. */
data class DefaultDropCycleValidator(var rules: MutableList<DefaultDropCycleRule>) {
    fun validate(item: DefaultDropCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultDropCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultDropCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultDropCycleValidator(): DefaultDropCycleValidator {
    return DefaultDropCycleValidator(
        mutableListOf<DefaultDropCycleRule>(
            DefaultDropCycleAllowedTrailerIdRule(mutableListOf<String>()),
            DefaultDropCycleRequiredTrailerIdRule("default drop cycle needs a value"),
            DefaultDropCycleRequiredLabelRule("default drop cycle needs this field"),
            DefaultDropCycleCodePrefixRule("X"),
            DefaultDropCycleRequiredShiftCodeRule("default drop cycle needs a value"),
            DefaultDropCycleRequiredCodeRule("default drop cycle needs a value"),
        ),
    )
}
