package com.acme.transport.customs.hold.adaptiveholdmapping

/** A rule a global hold policy must pass. Check returns its problems, or none. */
interface GlobalHoldPolicyRule {
    fun check(item: GlobalHoldPolicy): MutableList<String>
}

data class GlobalHoldPolicyRequiredDepotRule(var message: String) : GlobalHoldPolicyRule {
    override fun check(item: GlobalHoldPolicy): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalHoldPolicyNameLengthRule(var maxLength: Int) : GlobalHoldPolicyRule {
    override fun check(item: GlobalHoldPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalHoldPolicyCodeLengthRule(var maxLength: Int) : GlobalHoldPolicyRule {
    override fun check(item: GlobalHoldPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalHoldPolicyDepotLengthRule(var maxLength: Int) : GlobalHoldPolicyRule {
    override fun check(item: GlobalHoldPolicy): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalHoldPolicyVersionRangeRule(var min: Int, var max: Int) : GlobalHoldPolicyRule {
    override fun check(item: GlobalHoldPolicy): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global hold policy. */
data class GlobalHoldPolicyValidator(var rules: MutableList<GlobalHoldPolicyRule>) {
    fun validate(item: GlobalHoldPolicy): MutableList<String> {
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

    fun requireValid(item: GlobalHoldPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalHoldPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalHoldPolicyValidator(): GlobalHoldPolicyValidator {
    return GlobalHoldPolicyValidator(
        mutableListOf<GlobalHoldPolicyRule>(
            GlobalHoldPolicyRequiredDepotRule("global hold policy needs this field"),
            GlobalHoldPolicyNameLengthRule(32),
            GlobalHoldPolicyCodeLengthRule(1000),
            GlobalHoldPolicyDepotLengthRule(1000),
            GlobalHoldPolicyVersionRangeRule(32, 1000),
        ),
    )
}
