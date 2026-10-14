package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.text.normalize

/** A rule a defect score must pass. Check returns its problems, or none. */
interface DefectScoreRule {
    fun check(item: DefectScore): MutableList<String>
}

data class DefectScoreCreatedAtRangeRule(var min: Int, var max: Int) : DefectScoreRule {
    override fun check(item: DefectScore): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectScoreCodeLengthRule(var maxLength: Int) : DefectScoreRule {
    override fun check(item: DefectScore): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectScoreAllowedCodeRule(var allowed: MutableList<String>) : DefectScoreRule {
    override fun check(item: DefectScore): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class DefectScoreCodePrefixRule(var prefix: String) : DefectScoreRule {
    override fun check(item: DefectScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefectScoreRequiredCodeRule(var message: String) : DefectScoreRule {
    override fun check(item: DefectScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect score. */
data class DefectScoreValidator(var rules: MutableList<DefectScoreRule>) {
    fun validate(item: DefectScore): MutableList<String> {
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

    fun requireValid(item: DefectScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectScore>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectScoreValidator(): DefectScoreValidator {
    return DefectScoreValidator(
        mutableListOf<DefectScoreRule>(
            DefectScoreCreatedAtRangeRule(255, 3600),
            DefectScoreCodeLengthRule(3600),
            DefectScoreAllowedCodeRule(mutableListOf<String>("manual")),
            DefectScoreCodePrefixRule("OPS"),
            DefectScoreRequiredCodeRule("defect score needs an entry"),
        ),
    )
}
