package com.acme.transport.linehaul.laneplan.summary

import com.acme.transport.kit.text.normalize

/** A rule a batched lane plan bundle must pass. Check returns its problems, or none. */
interface BatchedLanePlanBundleRule {
    fun check(item: BatchedLanePlanBundle): MutableList<String>
}

data class BatchedLanePlanBundleRequiredCodeRule(var message: String) : BatchedLanePlanBundleRule {
    override fun check(item: BatchedLanePlanBundle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedLanePlanBundleTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : BatchedLanePlanBundleRule {
    override fun check(item: BatchedLanePlanBundle): MutableList<String> {
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

data class BatchedLanePlanBundleCodeLengthRule(var maxLength: Int) : BatchedLanePlanBundleRule {
    override fun check(item: BatchedLanePlanBundle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLanePlanBundleAllowedTitleRule(var allowed: MutableList<String>) : BatchedLanePlanBundleRule {
    override fun check(item: BatchedLanePlanBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

data class BatchedLanePlanBundleWindowDaysRangeRule(var min: Int, var max: Int) : BatchedLanePlanBundleRule {
    override fun check(item: BatchedLanePlanBundle): MutableList<String> {
        val value = item.windowDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("window days $value is below $min")
        }
        if (value > max) {
            problems.add("window days $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a batched lane plan bundle. */
data class BatchedLanePlanBundleValidator(var rules: MutableList<BatchedLanePlanBundleRule>) {
    fun validate(item: BatchedLanePlanBundle): MutableList<String> {
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

    fun requireValid(item: BatchedLanePlanBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedLanePlanBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedLanePlanBundleValidator(): BatchedLanePlanBundleValidator {
    return BatchedLanePlanBundleValidator(
        mutableListOf<BatchedLanePlanBundleRule>(
            BatchedLanePlanBundleRequiredCodeRule("batched lane plan bundle needs an entry"),
            BatchedLanePlanBundleTagsLimitRule(32, mutableListOf<String>("adaptive", "weighted", "fallback")),
            BatchedLanePlanBundleCodeLengthRule(255),
            BatchedLanePlanBundleAllowedTitleRule(mutableListOf<String>()),
            BatchedLanePlanBundleWindowDaysRangeRule(32, 120),
        ),
    )
}
