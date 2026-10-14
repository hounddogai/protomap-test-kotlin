package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.text.normalize

/** A rule a rolling dwell cycle must pass. Check returns its problems, or none. */
interface RollingDwellCycleRule {
    fun check(item: RollingDwellCycle): MutableList<String>
}

data class RollingDwellCycleRequiredCodeRule(var message: String) : RollingDwellCycleRule {
    override fun check(item: RollingDwellCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDwellCycleTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RollingDwellCycleRule {
    override fun check(item: RollingDwellCycle): MutableList<String> {
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

data class RollingDwellCycleAllowedTitleRule(var allowed: MutableList<String>) : RollingDwellCycleRule {
    override fun check(item: RollingDwellCycle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is too long")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling dwell cycle. */
data class RollingDwellCycleValidator(var rules: MutableList<RollingDwellCycleRule>) {
    fun validate(item: RollingDwellCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDwellCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDwellCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDwellCycleValidator(): RollingDwellCycleValidator {
    return RollingDwellCycleValidator(
        mutableListOf<RollingDwellCycleRule>(
            RollingDwellCycleRequiredCodeRule("rolling dwell cycle needs an entry"),
            RollingDwellCycleTagsLimitRule(255, mutableListOf<String>("tiered", "manual")),
            RollingDwellCycleAllowedTitleRule(mutableListOf<String>("rolling", "adaptive", "global")),
        ),
    )
}
