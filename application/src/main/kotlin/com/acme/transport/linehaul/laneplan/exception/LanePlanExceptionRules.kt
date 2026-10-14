package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.text.normalize

/** A rule a lane plan exception must pass. Check returns its problems, or none. */
interface LanePlanExceptionRule {
    fun check(item: LanePlanException): MutableList<String>
}

data class LanePlanExceptionAllowedCodeRule(var allowed: MutableList<String>) : LanePlanExceptionRule {
    override fun check(item: LanePlanException): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanExceptionScoreRangeRule(var min: Double, var max: Double) : LanePlanExceptionRule {
    override fun check(item: LanePlanException): MutableList<String> {
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

data class LanePlanExceptionRequiredRouteCodeRule(var message: String) : LanePlanExceptionRule {
    override fun check(item: LanePlanException): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanExceptionRequiredTitleRule(var message: String) : LanePlanExceptionRule {
    override fun check(item: LanePlanException): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanExceptionCreatedAtRangeRule(var min: Int, var max: Int) : LanePlanExceptionRule {
    override fun check(item: LanePlanException): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lane plan exception. */
data class LanePlanExceptionValidator(var rules: MutableList<LanePlanExceptionRule>) {
    fun validate(item: LanePlanException): MutableList<String> {
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

    fun requireValid(item: LanePlanException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanException>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanExceptionValidator(): LanePlanExceptionValidator {
    return LanePlanExceptionValidator(
        mutableListOf<LanePlanExceptionRule>(
            LanePlanExceptionAllowedCodeRule(mutableListOf<String>()),
            LanePlanExceptionScoreRangeRule(1.3, 4423.3),
            LanePlanExceptionRequiredRouteCodeRule("lane plan exception needs this field"),
            LanePlanExceptionRequiredTitleRule("lane plan exception needs this field"),
            LanePlanExceptionCreatedAtRangeRule(64, 1000),
        ),
    )
}
