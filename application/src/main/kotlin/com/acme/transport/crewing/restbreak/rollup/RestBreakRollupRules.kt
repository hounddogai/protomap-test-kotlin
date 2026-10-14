package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.text.normalize

/** A rule a rest break rollup must pass. Check returns its problems, or none. */
interface RestBreakRollupRule {
    fun check(item: RestBreakRollup): MutableList<String>
}

data class RestBreakRollupCodeLengthRule(var maxLength: Int) : RestBreakRollupRule {
    override fun check(item: RestBreakRollup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakRollupRequiredCodeRule(var message: String) : RestBreakRollupRule {
    override fun check(item: RestBreakRollup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakRollupOwnerIdLengthRule(var maxLength: Int) : RestBreakRollupRule {
    override fun check(item: RestBreakRollup): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakRollupRequiredLabelRule(var message: String) : RestBreakRollupRule {
    override fun check(item: RestBreakRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RestBreakRollupLabelLengthRule(var maxLength: Int) : RestBreakRollupRule {
    override fun check(item: RestBreakRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RestBreakRollupAllowedLabelRule(var allowed: MutableList<String>) : RestBreakRollupRule {
    override fun check(item: RestBreakRollup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rest break rollup. */
data class RestBreakRollupValidator(var rules: MutableList<RestBreakRollupRule>) {
    fun validate(item: RestBreakRollup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RestBreakRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RestBreakRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultRestBreakRollupValidator(): RestBreakRollupValidator {
    return RestBreakRollupValidator(
        mutableListOf<RestBreakRollupRule>(
            RestBreakRollupCodeLengthRule(32),
            RestBreakRollupRequiredCodeRule("rest break rollup needs this field"),
            RestBreakRollupOwnerIdLengthRule(86400),
            RestBreakRollupRequiredLabelRule("rest break rollup needs a value"),
            RestBreakRollupLabelLengthRule(86400),
            RestBreakRollupAllowedLabelRule(mutableListOf<String>("batched")),
        ),
    )
}
