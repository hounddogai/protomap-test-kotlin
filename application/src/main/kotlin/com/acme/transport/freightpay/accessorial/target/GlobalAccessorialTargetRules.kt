package com.acme.transport.freightpay.accessorial.target

import com.acme.transport.kit.text.normalize

/** A rule a global accessorial target must pass. Check returns its problems, or none. */
interface GlobalAccessorialTargetRule {
    fun check(item: GlobalAccessorialTarget): MutableList<String>
}

data class GlobalAccessorialTargetAllowedNotesRule(
    var allowed: MutableList<String>,
) : GlobalAccessorialTargetRule {
    override fun check(item: GlobalAccessorialTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAccessorialTargetScoreRangeRule(
    var min: Double,
    var max: Double,
) : GlobalAccessorialTargetRule {
    override fun check(item: GlobalAccessorialTarget): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAccessorialTargetRequiredCodeRule(var message: String) : GlobalAccessorialTargetRule {
    override fun check(item: GlobalAccessorialTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAccessorialTargetCodePrefixRule(var prefix: String) : GlobalAccessorialTargetRule {
    override fun check(item: GlobalAccessorialTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAccessorialTargetNameLengthRule(var maxLength: Int) : GlobalAccessorialTargetRule {
    override fun check(item: GlobalAccessorialTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global accessorial target. */
data class GlobalAccessorialTargetValidator(var rules: MutableList<GlobalAccessorialTargetRule>) {
    fun validate(item: GlobalAccessorialTarget): MutableList<String> {
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

    fun requireValid(item: GlobalAccessorialTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAccessorialTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAccessorialTargetValidator(): GlobalAccessorialTargetValidator {
    return GlobalAccessorialTargetValidator(
        mutableListOf<GlobalAccessorialTargetRule>(
            GlobalAccessorialTargetAllowedNotesRule(mutableListOf<String>("tiered", "capped", "global")),
            GlobalAccessorialTargetScoreRangeRule(1.7, 127.0),
            GlobalAccessorialTargetRequiredCodeRule("global accessorial target needs this field"),
            GlobalAccessorialTargetCodePrefixRule("Q"),
            GlobalAccessorialTargetNameLengthRule(80),
        ),
    )
}
