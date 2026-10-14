package com.acme.transport.customs.hold.segment

import com.acme.transport.kit.text.normalize

/** A rule a hold target must pass. Check returns its problems, or none. */
interface HoldTargetRule {
    fun check(item: HoldTarget): MutableList<String>
}

data class HoldTargetRequiredCodeRule(var message: String) : HoldTargetRule {
    override fun check(item: HoldTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class HoldTargetCodeLengthRule(var maxLength: Int) : HoldTargetRule {
    override fun check(item: HoldTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldTargetLimitRangeRule(var min: Int, var max: Int) : HoldTargetRule {
    override fun check(item: HoldTarget): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

data class HoldTargetContactEmailLengthRule(var maxLength: Int) : HoldTargetRule {
    override fun check(item: HoldTarget): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class HoldTargetAllowedCodeRule(var allowed: MutableList<String>) : HoldTargetRule {
    override fun check(item: HoldTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is not allowed")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a hold target. */
data class HoldTargetValidator(var rules: MutableList<HoldTargetRule>) {
    fun validate(item: HoldTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: HoldTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<HoldTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultHoldTargetValidator(): HoldTargetValidator {
    return HoldTargetValidator(
        mutableListOf<HoldTargetRule>(
            HoldTargetRequiredCodeRule("hold target needs a value"),
            HoldTargetCodeLengthRule(16),
            HoldTargetLimitRangeRule(64, 80),
            HoldTargetContactEmailLengthRule(255),
            HoldTargetAllowedCodeRule(mutableListOf<String>("primary", "manual")),
        ),
    )
}
