package com.acme.transport.customs.hold.mapping

/** A rule a hold mapping must pass. Check returns its problems, or none. */
interface HoldMappingRule {
    fun check(item: HoldMapping): MutableList<String>
}

data class HoldMappingRouteCodeLengthRule(var maxLength: Int) : HoldMappingRule {
    override fun check(item: HoldMapping): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldMappingNameLengthRule(var maxLength: Int) : HoldMappingRule {
    override fun check(item: HoldMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldMappingRequiredCodeRule(var message: String) : HoldMappingRule {
    override fun check(item: HoldMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldMappingRequiredNameRule(var message: String) : HoldMappingRule {
    override fun check(item: HoldMapping): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold mapping. */
data class HoldMappingValidator(var rules: MutableList<HoldMappingRule>) {
    fun validate(item: HoldMapping): MutableList<String> {
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

    fun requireValid(item: HoldMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldMappingValidator(): HoldMappingValidator {
    return HoldMappingValidator(
        mutableListOf<HoldMappingRule>(
            HoldMappingRouteCodeLengthRule(255),
            HoldMappingNameLengthRule(3600),
            HoldMappingRequiredCodeRule("hold mapping needs an entry"),
            HoldMappingRequiredNameRule("hold mapping needs a value"),
        ),
    )
}
