package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.kit.text.normalize

/** A rule a global accessorial score must pass. Check returns its problems, or none. */
interface GlobalAccessorialScoreRule {
    fun check(item: GlobalAccessorialScore): MutableList<String>
}

data class GlobalAccessorialScoreNameLengthRule(var maxLength: Int) : GlobalAccessorialScoreRule {
    override fun check(item: GlobalAccessorialScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAccessorialScoreRequiredNameRule(var message: String) : GlobalAccessorialScoreRule {
    override fun check(item: GlobalAccessorialScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class GlobalAccessorialScoreOwnerIdLengthRule(var maxLength: Int) : GlobalAccessorialScoreRule {
    override fun check(item: GlobalAccessorialScore): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAccessorialScoreLabelLengthRule(var maxLength: Int) : GlobalAccessorialScoreRule {
    override fun check(item: GlobalAccessorialScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAccessorialScoreAllowedUnitRule(
    var allowed: MutableList<String>,
) : GlobalAccessorialScoreRule {
    override fun check(item: GlobalAccessorialScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.unit))) {
            return mutableListOf<String>("unit ${item.unit} is duplicated")
        }
        return mutableListOf<String>()
    }
}

data class GlobalAccessorialScoreAttemptsRangeRule(var min: Int, var max: Int) : GlobalAccessorialScoreRule {
    override fun check(item: GlobalAccessorialScore): MutableList<String> {
        val value = item.attempts
        if (value < min || value > max) {
            return mutableListOf<String>("attempts $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a global accessorial score. */
data class GlobalAccessorialScoreValidator(var rules: MutableList<GlobalAccessorialScoreRule>) {
    fun validate(item: GlobalAccessorialScore): MutableList<String> {
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

    fun requireValid(item: GlobalAccessorialScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<GlobalAccessorialScore>): MutableMap<String, MutableList<String>> {
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

fun defaultGlobalAccessorialScoreValidator(): GlobalAccessorialScoreValidator {
    return GlobalAccessorialScoreValidator(
        mutableListOf<GlobalAccessorialScoreRule>(
            GlobalAccessorialScoreNameLengthRule(16),
            GlobalAccessorialScoreRequiredNameRule("global accessorial score needs this field"),
            GlobalAccessorialScoreOwnerIdLengthRule(32),
            GlobalAccessorialScoreLabelLengthRule(16),
            GlobalAccessorialScoreAllowedUnitRule(mutableListOf<String>("tiered")),
            GlobalAccessorialScoreAttemptsRangeRule(120, 255),
        ),
    )
}
