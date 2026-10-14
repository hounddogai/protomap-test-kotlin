package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.kit.text.normalize

/** A rule a default accessorial cycle must pass. Check returns its problems, or none. */
interface DefaultAccessorialCycleRule {
    fun check(item: DefaultAccessorialCycle): MutableList<String>
}

data class DefaultAccessorialCycleCodeLengthRule(var maxLength: Int) : DefaultAccessorialCycleRule {
    override fun check(item: DefaultAccessorialCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAccessorialCycleVersionRangeRule(var min: Int, var max: Int) : DefaultAccessorialCycleRule {
    override fun check(item: DefaultAccessorialCycle): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

data class DefaultAccessorialCycleRouteCodeLengthRule(var maxLength: Int) : DefaultAccessorialCycleRule {
    override fun check(item: DefaultAccessorialCycle): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultAccessorialCycleFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : DefaultAccessorialCycleRule {
    override fun check(item: DefaultAccessorialCycle): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class DefaultAccessorialCycleTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : DefaultAccessorialCycleRule {
    override fun check(item: DefaultAccessorialCycle): MutableList<String> {
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

data class DefaultAccessorialCycleAllowedNameRule(
    var allowed: MutableList<String>,
) : DefaultAccessorialCycleRule {
    override fun check(item: DefaultAccessorialCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default accessorial cycle. */
data class DefaultAccessorialCycleValidator(var rules: MutableList<DefaultAccessorialCycleRule>) {
    fun validate(item: DefaultAccessorialCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultAccessorialCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultAccessorialCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultAccessorialCycleValidator(): DefaultAccessorialCycleValidator {
    return DefaultAccessorialCycleValidator(
        mutableListOf<DefaultAccessorialCycleRule>(
            DefaultAccessorialCycleCodeLengthRule(86400),
            DefaultAccessorialCycleVersionRangeRule(255, 86400),
            DefaultAccessorialCycleRouteCodeLengthRule(86400),
            DefaultAccessorialCycleFuelLitersRangeRule(3.4, 2421.9),
            DefaultAccessorialCycleTagsLimitRule(255, mutableListOf<String>("regional", "manual", "lenient")),
            DefaultAccessorialCycleAllowedNameRule(mutableListOf<String>("strict", "strict", "strict")),
        ),
    )
}
