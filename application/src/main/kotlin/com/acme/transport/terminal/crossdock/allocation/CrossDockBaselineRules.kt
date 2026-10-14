package com.acme.transport.terminal.crossdock.allocation

import com.acme.transport.kit.text.normalize

/** A rule a cross dock baseline must pass. Check returns its problems, or none. */
interface CrossDockBaselineRule {
    fun check(item: CrossDockBaseline): MutableList<String>
}

data class CrossDockBaselineNameLengthRule(var maxLength: Int) : CrossDockBaselineRule {
    override fun check(item: CrossDockBaseline): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockBaselineAllowedDescriptionRule(var allowed: MutableList<String>) : CrossDockBaselineRule {
    override fun check(item: CrossDockBaseline): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.description))) {
            return mutableListOf<String>("description ${item.description} is stale")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockBaselineRequiredDescriptionRule(var message: String) : CrossDockBaselineRule {
    override fun check(item: CrossDockBaseline): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock baseline. */
data class CrossDockBaselineValidator(var rules: MutableList<CrossDockBaselineRule>) {
    fun validate(item: CrossDockBaseline): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CrossDockBaseline) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockBaseline>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockBaselineValidator(): CrossDockBaselineValidator {
    return CrossDockBaselineValidator(
        mutableListOf<CrossDockBaselineRule>(
            CrossDockBaselineNameLengthRule(120),
            CrossDockBaselineAllowedDescriptionRule(mutableListOf<String>("capped")),
            CrossDockBaselineRequiredDescriptionRule("cross dock baseline needs a value"),
        ),
    )
}
