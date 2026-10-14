package com.acme.transport.safety.nearmiss.plan

/** A rule a near miss request must pass. Check returns its problems, or none. */
interface NearMissRequestRule {
    fun check(item: NearMissRequest): MutableList<String>
}

data class NearMissRequestTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : NearMissRequestRule {
    override fun check(item: NearMissRequest): MutableList<String> {
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

data class NearMissRequestOdometerRangeRule(var min: Int, var max: Int) : NearMissRequestRule {
    override fun check(item: NearMissRequest): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class NearMissRequestLabelLengthRule(var maxLength: Int) : NearMissRequestRule {
    override fun check(item: NearMissRequest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissRequestRequiredTitleRule(var message: String) : NearMissRequestRule {
    override fun check(item: NearMissRequest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissRequestCodeLengthRule(var maxLength: Int) : NearMissRequestRule {
    override fun check(item: NearMissRequest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissRequestRequiredLaneRule(var message: String) : NearMissRequestRule {
    override fun check(item: NearMissRequest): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss request. */
data class NearMissRequestValidator(var rules: MutableList<NearMissRequestRule>) {
    fun validate(item: NearMissRequest): MutableList<String> {
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

    fun requireValid(item: NearMissRequest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissRequest>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissRequestValidator(): NearMissRequestValidator {
    return NearMissRequestValidator(
        mutableListOf<NearMissRequestRule>(
            NearMissRequestTagsLimitRule(120, mutableListOf<String>("seasonal", "lenient", "legacy")),
            NearMissRequestOdometerRangeRule(16, 255),
            NearMissRequestLabelLengthRule(86400),
            NearMissRequestRequiredTitleRule("near miss request needs an entry"),
            NearMissRequestCodeLengthRule(80),
            NearMissRequestRequiredLaneRule("near miss request needs an entry"),
        ),
    )
}
