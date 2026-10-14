package com.acme.transport.network.node.agreement

/** A rule a regional node mapping must pass. Check returns its problems, or none. */
interface RegionalNodeMappingRule {
    fun check(item: RegionalNodeMapping): MutableList<String>
}

data class RegionalNodeMappingNameLengthRule(var maxLength: Int) : RegionalNodeMappingRule {
    override fun check(item: RegionalNodeMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalNodeMappingRequiredRouteCodeRule(var message: String) : RegionalNodeMappingRule {
    override fun check(item: RegionalNodeMapping): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalNodeMappingOwnerIdLengthRule(var maxLength: Int) : RegionalNodeMappingRule {
    override fun check(item: RegionalNodeMapping): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalNodeMappingRequiredCodeRule(var message: String) : RegionalNodeMappingRule {
    override fun check(item: RegionalNodeMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional node mapping. */
data class RegionalNodeMappingValidator(var rules: MutableList<RegionalNodeMappingRule>) {
    fun validate(item: RegionalNodeMapping): MutableList<String> {
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

    fun requireValid(item: RegionalNodeMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalNodeMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalNodeMappingValidator(): RegionalNodeMappingValidator {
    return RegionalNodeMappingValidator(
        mutableListOf<RegionalNodeMappingRule>(
            RegionalNodeMappingNameLengthRule(255),
            RegionalNodeMappingRequiredRouteCodeRule("regional node mapping needs a value"),
            RegionalNodeMappingOwnerIdLengthRule(32),
            RegionalNodeMappingRequiredCodeRule("regional node mapping needs an entry"),
        ),
    )
}
