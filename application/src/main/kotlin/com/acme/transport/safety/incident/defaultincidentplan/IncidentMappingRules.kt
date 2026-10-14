package com.acme.transport.safety.incident.defaultincidentplan

/** A rule a incident mapping must pass. Check returns its problems, or none. */
interface IncidentMappingRule {
    fun check(item: IncidentMapping): MutableList<String>
}

data class IncidentMappingExternalRefLengthRule(var maxLength: Int) : IncidentMappingRule {
    override fun check(item: IncidentMapping): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentMappingCostRangeRule(var min: Double, var max: Double) : IncidentMappingRule {
    override fun check(item: IncidentMapping): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class IncidentMappingCountryLengthRule(var maxLength: Int) : IncidentMappingRule {
    override fun check(item: IncidentMapping): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class IncidentMappingCodePrefixRule(var prefix: String) : IncidentMappingRule {
    override fun check(item: IncidentMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class IncidentMappingBatchSizeRangeRule(var min: Int, var max: Int) : IncidentMappingRule {
    override fun check(item: IncidentMapping): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a incident mapping. */
data class IncidentMappingValidator(var rules: MutableList<IncidentMappingRule>) {
    fun validate(item: IncidentMapping): MutableList<String> {
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

    fun requireValid(item: IncidentMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<IncidentMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultIncidentMappingValidator(): IncidentMappingValidator {
    return IncidentMappingValidator(
        mutableListOf<IncidentMappingRule>(
            IncidentMappingExternalRefLengthRule(3600),
            IncidentMappingCostRangeRule(2.0, 3716.4),
            IncidentMappingCountryLengthRule(64),
            IncidentMappingCodePrefixRule("X"),
            IncidentMappingBatchSizeRangeRule(32, 3600),
        ),
    )
}
