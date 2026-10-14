package com.acme.transport.fleetops.defect.mapping

/** A rule a defect mapping must pass. Check returns its problems, or none. */
interface DefectMappingRule {
    fun check(item: DefectMapping): MutableList<String>
}

data class DefectMappingEndsAtRangeRule(var min: Int, var max: Int) : DefectMappingRule {
    override fun check(item: DefectMapping): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class DefectMappingRevisionRangeRule(var min: Int, var max: Int) : DefectMappingRule {
    override fun check(item: DefectMapping): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectMappingCodePrefixRule(var prefix: String) : DefectMappingRule {
    override fun check(item: DefectMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefectMappingApprovedGuardRule(var limit: Int) : DefectMappingRule {
    override fun check(item: DefectMapping): MutableList<String> {
        if (item.approved && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class DefectMappingMinimumRangeRule(var min: Double, var max: Double) : DefectMappingRule {
    override fun check(item: DefectMapping): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a defect mapping. */
data class DefectMappingValidator(var rules: MutableList<DefectMappingRule>) {
    fun validate(item: DefectMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectMappingValidator(): DefectMappingValidator {
    return DefectMappingValidator(
        mutableListOf<DefectMappingRule>(
            DefectMappingEndsAtRangeRule(64, 120),
            DefectMappingRevisionRangeRule(1000, 86400),
            DefectMappingCodePrefixRule("ACM"),
            DefectMappingApprovedGuardRule(3600),
            DefectMappingMinimumRangeRule(1.1, 188.8),
        ),
    )
}
