package com.acme.transport.terminal.dockdoor.snapshot

import com.acme.transport.kit.text.normalize

/** A rule a fallback dock door exception must pass. Check returns its problems, or none. */
interface FallbackDockDoorExceptionRule {
    fun check(item: FallbackDockDoorException): MutableList<String>
}

data class FallbackDockDoorExceptionNameLengthRule(var maxLength: Int) : FallbackDockDoorExceptionRule {
    override fun check(item: FallbackDockDoorException): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDockDoorExceptionAllowedCodeRule(
    var allowed: MutableList<String>,
) : FallbackDockDoorExceptionRule {
    override fun check(item: FallbackDockDoorException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class FallbackDockDoorExceptionRequiredLaneRule(var message: String) : FallbackDockDoorExceptionRule {
    override fun check(item: FallbackDockDoorException): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback dock door exception. */
data class FallbackDockDoorExceptionValidator(var rules: MutableList<FallbackDockDoorExceptionRule>) {
    fun validate(item: FallbackDockDoorException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackDockDoorException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackDockDoorException>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackDockDoorExceptionValidator(): FallbackDockDoorExceptionValidator {
    return FallbackDockDoorExceptionValidator(
        mutableListOf<FallbackDockDoorExceptionRule>(
            FallbackDockDoorExceptionNameLengthRule(120),
            FallbackDockDoorExceptionAllowedCodeRule(mutableListOf<String>("weighted")),
            FallbackDockDoorExceptionRequiredLaneRule("fallback dock door exception needs this field"),
        ),
    )
}
