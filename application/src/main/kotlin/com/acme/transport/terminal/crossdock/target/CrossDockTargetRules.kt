package com.acme.transport.terminal.crossdock.target

import com.acme.transport.kit.text.normalize

/** A rule a cross dock target must pass. Check returns its problems, or none. */
interface CrossDockTargetRule {
    fun check(item: CrossDockTarget): MutableList<String>
}

data class CrossDockTargetPinnedGuardRule(var limit: Double) : CrossDockTargetRule {
    override fun check(item: CrossDockTarget): MutableList<String> {
        if (item.pinned && item.maximum > limit) {
            return mutableListOf<String>("maximum ${item.maximum} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockTargetRequiredCodeRule(var message: String) : CrossDockTargetRule {
    override fun check(item: CrossDockTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CrossDockTargetAllowedCodeRule(var allowed: MutableList<String>) : CrossDockTargetRule {
    override fun check(item: CrossDockTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockTargetDepotLengthRule(var maxLength: Int) : CrossDockTargetRule {
    override fun check(item: CrossDockTarget): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CrossDockTargetRegionLengthRule(var maxLength: Int) : CrossDockTargetRule {
    override fun check(item: CrossDockTarget): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cross dock target. */
data class CrossDockTargetValidator(var rules: MutableList<CrossDockTargetRule>) {
    fun validate(item: CrossDockTarget): MutableList<String> {
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

    fun requireValid(item: CrossDockTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CrossDockTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultCrossDockTargetValidator(): CrossDockTargetValidator {
    return CrossDockTargetValidator(
        mutableListOf<CrossDockTargetRule>(
            CrossDockTargetPinnedGuardRule(0.9),
            CrossDockTargetRequiredCodeRule("cross dock target needs an entry"),
            CrossDockTargetAllowedCodeRule(mutableListOf<String>("pending", "seasonal", "strict")),
            CrossDockTargetDepotLengthRule(80),
            CrossDockTargetRegionLengthRule(255),
        ),
    )
}
