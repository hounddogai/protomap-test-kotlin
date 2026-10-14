package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.kit.text.normalize

/** A rule a strict dock door policy must pass. Check returns its problems, or none. */
interface StrictDockDoorPolicyRule {
    fun check(item: StrictDockDoorPolicy): MutableList<String>
}

data class StrictDockDoorPolicyCodeLengthRule(var maxLength: Int) : StrictDockDoorPolicyRule {
    override fun check(item: StrictDockDoorPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorPolicyNameLengthRule(var maxLength: Int) : StrictDockDoorPolicyRule {
    override fun check(item: StrictDockDoorPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorPolicyRequiredNameRule(var message: String) : StrictDockDoorPolicyRule {
    override fun check(item: StrictDockDoorPolicy): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDockDoorPolicyAllowedNameRule(var allowed: MutableList<String>) : StrictDockDoorPolicyRule {
    override fun check(item: StrictDockDoorPolicy): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict dock door policy. */
data class StrictDockDoorPolicyValidator(var rules: MutableList<StrictDockDoorPolicyRule>) {
    fun validate(item: StrictDockDoorPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictDockDoorPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDockDoorPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDockDoorPolicyValidator(): StrictDockDoorPolicyValidator {
    return StrictDockDoorPolicyValidator(
        mutableListOf<StrictDockDoorPolicyRule>(
            StrictDockDoorPolicyCodeLengthRule(1000),
            StrictDockDoorPolicyNameLengthRule(80),
            StrictDockDoorPolicyRequiredNameRule("strict dock door policy needs an entry"),
            StrictDockDoorPolicyAllowedNameRule(mutableListOf<String>("scheduled", "manual", "tiered")),
        ),
    )
}
