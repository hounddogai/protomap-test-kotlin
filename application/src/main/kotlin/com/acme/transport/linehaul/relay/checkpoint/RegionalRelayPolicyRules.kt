package com.acme.transport.linehaul.relay.checkpoint

/** A rule a regional relay policy must pass. Check returns its problems, or none. */
interface RegionalRelayPolicyRule {
    fun check(item: RegionalRelayPolicy): MutableList<String>
}

data class RegionalRelayPolicyCodeLengthRule(var maxLength: Int) : RegionalRelayPolicyRule {
    override fun check(item: RegionalRelayPolicy): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRelayPolicyNameLengthRule(var maxLength: Int) : RegionalRelayPolicyRule {
    override fun check(item: RegionalRelayPolicy): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRelayPolicyRouteCodeLengthRule(var maxLength: Int) : RegionalRelayPolicyRule {
    override fun check(item: RegionalRelayPolicy): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalRelayPolicyRequiredDriverIdRule(var message: String) : RegionalRelayPolicyRule {
    override fun check(item: RegionalRelayPolicy): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalRelayPolicyRequiredCodeRule(var message: String) : RegionalRelayPolicyRule {
    override fun check(item: RegionalRelayPolicy): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional relay policy. */
data class RegionalRelayPolicyValidator(var rules: MutableList<RegionalRelayPolicyRule>) {
    fun validate(item: RegionalRelayPolicy): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalRelayPolicy) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalRelayPolicy>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalRelayPolicyValidator(): RegionalRelayPolicyValidator {
    return RegionalRelayPolicyValidator(
        mutableListOf<RegionalRelayPolicyRule>(
            RegionalRelayPolicyCodeLengthRule(86400),
            RegionalRelayPolicyNameLengthRule(86400),
            RegionalRelayPolicyRouteCodeLengthRule(1000),
            RegionalRelayPolicyRequiredDriverIdRule("regional relay policy needs this field"),
            RegionalRelayPolicyRequiredCodeRule("regional relay policy needs a value"),
        ),
    )
}
