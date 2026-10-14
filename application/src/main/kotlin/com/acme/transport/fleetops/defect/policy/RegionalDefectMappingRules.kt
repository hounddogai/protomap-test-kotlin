package com.acme.transport.fleetops.defect.policy

/** A rule a regional defect mapping must pass. Check returns its problems, or none. */
interface RegionalDefectMappingRule {
    fun check(item: RegionalDefectMapping): MutableList<String>
}

data class RegionalDefectMappingCodeLengthRule(var maxLength: Int) : RegionalDefectMappingRule {
    override fun check(item: RegionalDefectMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectMappingTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RegionalDefectMappingRule {
    override fun check(item: RegionalDefectMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class RegionalDefectMappingCodePrefixRule(var prefix: String) : RegionalDefectMappingRule {
    override fun check(item: RegionalDefectMapping): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDefectMappingTimezoneLengthRule(var maxLength: Int) : RegionalDefectMappingRule {
    override fun check(item: RegionalDefectMapping): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional defect mapping. */
data class RegionalDefectMappingValidator(var rules: MutableList<RegionalDefectMappingRule>) {
    fun validate(item: RegionalDefectMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDefectMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDefectMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDefectMappingValidator(): RegionalDefectMappingValidator {
    return RegionalDefectMappingValidator(
        mutableListOf<RegionalDefectMappingRule>(
            RegionalDefectMappingCodeLengthRule(255),
            RegionalDefectMappingTagsLimitRule(16, mutableListOf<String>("lenient", "pending")),
            RegionalDefectMappingCodePrefixRule("ACM"),
            RegionalDefectMappingTimezoneLengthRule(120),
        ),
    )
}
