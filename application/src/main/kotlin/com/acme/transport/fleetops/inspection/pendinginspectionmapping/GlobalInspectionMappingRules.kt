package com.acme.transport.fleetops.inspection.pendinginspectionmapping

/** A rule a global inspection mapping must pass. Check returns its problems, or none. */
interface GlobalInspectionMappingRule {
    fun check(item: GlobalInspectionMapping): MutableList<String>
}

data class GlobalInspectionMappingCodePrefixRule(var prefix: String) : GlobalInspectionMappingRule {
    override fun check(item: GlobalInspectionMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionMappingRequiredNameRule(var message: String) : GlobalInspectionMappingRule {
    override fun check(item: GlobalInspectionMapping): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalInspectionMappingPalletsRangeRule(var min: Int, var max: Int) : GlobalInspectionMappingRule {
    override fun check(item: GlobalInspectionMapping): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class GlobalInspectionMappingNameLengthRule(var maxLength: Int) : GlobalInspectionMappingRule {
    override fun check(item: GlobalInspectionMapping): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global inspection mapping. */
data class GlobalInspectionMappingValidator(var rules: MutableList<GlobalInspectionMappingRule>) {
    fun validate(item: GlobalInspectionMapping): MutableList<String> {
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

    fun requireValid(item: GlobalInspectionMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalInspectionMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalInspectionMappingValidator(): GlobalInspectionMappingValidator {
    return GlobalInspectionMappingValidator(
        mutableListOf<GlobalInspectionMappingRule>(
            GlobalInspectionMappingCodePrefixRule("Q"),
            GlobalInspectionMappingRequiredNameRule("global inspection mapping needs a value"),
            GlobalInspectionMappingPalletsRangeRule(32, 255),
            GlobalInspectionMappingNameLengthRule(1000),
        ),
    )
}
