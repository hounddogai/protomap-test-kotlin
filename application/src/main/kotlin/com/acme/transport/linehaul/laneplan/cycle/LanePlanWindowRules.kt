package com.acme.transport.linehaul.laneplan.cycle

import com.acme.transport.kit.text.normalize

/** A rule a lane plan window must pass. Check returns its problems, or none. */
interface LanePlanWindowRule {
    fun check(item: LanePlanWindow): MutableList<String>
}

data class LanePlanWindowSourceLengthRule(var maxLength: Int) : LanePlanWindowRule {
    override fun check(item: LanePlanWindow): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanWindowRequiredCodeRule(var message: String) : LanePlanWindowRule {
    override fun check(item: LanePlanWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanWindowAllowedCodeRule(var allowed: MutableList<String>) : LanePlanWindowRule {
    override fun check(item: LanePlanWindow): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is expired")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanWindowFuelLitersRangeRule(var min: Double, var max: Double) : LanePlanWindowRule {
    override fun check(item: LanePlanWindow): MutableList<String> {
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

data class LanePlanWindowRequiredSourceRule(var message: String) : LanePlanWindowRule {
    override fun check(item: LanePlanWindow): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanWindowRequiredNameRule(var message: String) : LanePlanWindowRule {
    override fun check(item: LanePlanWindow): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan window. */
data class LanePlanWindowValidator(var rules: MutableList<LanePlanWindowRule>) {
    fun validate(item: LanePlanWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LanePlanWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanWindowValidator(): LanePlanWindowValidator {
    return LanePlanWindowValidator(
        mutableListOf<LanePlanWindowRule>(
            LanePlanWindowSourceLengthRule(64),
            LanePlanWindowRequiredCodeRule("lane plan window needs this field"),
            LanePlanWindowAllowedCodeRule(mutableListOf<String>("pending", "rolling")),
            LanePlanWindowFuelLitersRangeRule(0.6, 656.8),
            LanePlanWindowRequiredSourceRule("lane plan window needs this field"),
            LanePlanWindowRequiredNameRule("lane plan window needs this field"),
        ),
    )
}
