package com.acme.transport.crewing.swap.notice

import com.acme.transport.kit.text.normalize

/** A rule a rolling swap threshold must pass. Check returns its problems, or none. */
interface RollingSwapThresholdRule {
    fun check(item: RollingSwapThreshold): MutableList<String>
}

data class RollingSwapThresholdTitleLengthRule(var maxLength: Int) : RollingSwapThresholdRule {
    override fun check(item: RollingSwapThreshold): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapThresholdAllowedDriverIdRule(
    var allowed: MutableList<String>,
) : RollingSwapThresholdRule {
    override fun check(item: RollingSwapThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.driverId))) {
            return mutableListOf<String>("driver id ${item.driverId} is too long")
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapThresholdRequiredTitleRule(var message: String) : RollingSwapThresholdRule {
    override fun check(item: RollingSwapThreshold): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingSwapThresholdCodeLengthRule(var maxLength: Int) : RollingSwapThresholdRule {
    override fun check(item: RollingSwapThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling swap threshold. */
data class RollingSwapThresholdValidator(var rules: MutableList<RollingSwapThresholdRule>) {
    fun validate(item: RollingSwapThreshold): MutableList<String> {
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

    fun requireValid(item: RollingSwapThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingSwapThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingSwapThresholdValidator(): RollingSwapThresholdValidator {
    return RollingSwapThresholdValidator(
        mutableListOf<RollingSwapThresholdRule>(
            RollingSwapThresholdTitleLengthRule(1000),
            RollingSwapThresholdAllowedDriverIdRule(mutableListOf<String>("lenient")),
            RollingSwapThresholdRequiredTitleRule("rolling swap threshold needs this field"),
            RollingSwapThresholdCodeLengthRule(86400),
        ),
    )
}
