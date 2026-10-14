package com.acme.transport.customs.inspection.inspectiondigest

/** A rule a fallback inspection request must pass. Check returns its problems, or none. */
interface FallbackInspectionRequestRule {
    fun check(item: FallbackInspectionRequest): MutableList<String>
}

data class FallbackInspectionRequestRequiredCodeRule(var message: String) : FallbackInspectionRequestRule {
    override fun check(item: FallbackInspectionRequest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionRequestScoreRangeRule(
    var min: Double,
    var max: Double,
) : FallbackInspectionRequestRule {
    override fun check(item: FallbackInspectionRequest): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class FallbackInspectionRequestTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : FallbackInspectionRequestRule {
    override fun check(item: FallbackInspectionRequest): MutableList<String> {
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

data class FallbackInspectionRequestRequiredDepotRule(var message: String) : FallbackInspectionRequestRule {
    override fun check(item: FallbackInspectionRequest): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackInspectionRequestCodeLengthRule(var maxLength: Int) : FallbackInspectionRequestRule {
    override fun check(item: FallbackInspectionRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback inspection request. */
data class FallbackInspectionRequestValidator(var rules: MutableList<FallbackInspectionRequestRule>) {
    fun validate(item: FallbackInspectionRequest): MutableList<String> {
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

    fun requireValid(item: FallbackInspectionRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackInspectionRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackInspectionRequestValidator(): FallbackInspectionRequestValidator {
    return FallbackInspectionRequestValidator(
        mutableListOf<FallbackInspectionRequestRule>(
            FallbackInspectionRequestRequiredCodeRule("fallback inspection request needs an entry"),
            FallbackInspectionRequestScoreRangeRule(9.4, 3071.3),
            FallbackInspectionRequestTagsLimitRule(64, mutableListOf<String>("batched")),
            FallbackInspectionRequestRequiredDepotRule("fallback inspection request needs this field"),
            FallbackInspectionRequestCodeLengthRule(120),
        ),
    )
}
