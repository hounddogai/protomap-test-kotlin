package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.kit.text.normalize

/** A rule a defect group must pass. Check returns its problems, or none. */
interface DefectGroupRule {
    fun check(item: DefectGroup): MutableList<String>
}

data class DefectGroupAllowedTitleRule(var allowed: MutableList<String>) : DefectGroupRule {
    override fun check(item: DefectGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class DefectGroupRatioRangeRule(var min: Double, var max: Double) : DefectGroupRule {
    override fun check(item: DefectGroup): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectGroupCodeLengthRule(var maxLength: Int) : DefectGroupRule {
    override fun check(item: DefectGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect group. */
data class DefectGroupValidator(var rules: MutableList<DefectGroupRule>) {
    fun validate(item: DefectGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectGroupValidator(): DefectGroupValidator {
    return DefectGroupValidator(
        mutableListOf<DefectGroupRule>(
            DefectGroupAllowedTitleRule(mutableListOf<String>("manual")),
            DefectGroupRatioRangeRule(1.8, 2005.5),
            DefectGroupCodeLengthRule(255),
        ),
    )
}
