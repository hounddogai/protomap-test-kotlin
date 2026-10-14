package com.acme.transport.network.node.group

/** A rule a primary node profile must pass. Check returns its problems, or none. */
interface PrimaryNodeProfileRule {
    fun check(item: PrimaryNodeProfile): MutableList<String>
}

data class PrimaryNodeProfileChannelLengthRule(var maxLength: Int) : PrimaryNodeProfileRule {
    override fun check(item: PrimaryNodeProfile): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNodeProfileBudgetRangeRule(var min: Double, var max: Double) : PrimaryNodeProfileRule {
    override fun check(item: PrimaryNodeProfile): MutableList<String> {
        val value = item.budget
        if (value < min || value > max) {
            return mutableListOf<String>("budget $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNodeProfileCodeLengthRule(var maxLength: Int) : PrimaryNodeProfileRule {
    override fun check(item: PrimaryNodeProfile): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryNodeProfileTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : PrimaryNodeProfileRule {
    override fun check(item: PrimaryNodeProfile): MutableList<String> {
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

/** Runs every rule of a primary node profile. */
data class PrimaryNodeProfileValidator(var rules: MutableList<PrimaryNodeProfileRule>) {
    fun validate(item: PrimaryNodeProfile): MutableList<String> {
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

    fun requireValid(item: PrimaryNodeProfile) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryNodeProfile>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryNodeProfileValidator(): PrimaryNodeProfileValidator {
    return PrimaryNodeProfileValidator(
        mutableListOf<PrimaryNodeProfileRule>(
            PrimaryNodeProfileChannelLengthRule(120),
            PrimaryNodeProfileBudgetRangeRule(8.5, 3105.6),
            PrimaryNodeProfileCodeLengthRule(86400),
            PrimaryNodeProfileTagsLimitRule(1000, mutableListOf<String>("legacy", "seasonal")),
        ),
    )
}
