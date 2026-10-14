package com.acme.transport.fleetops.defect.defecttarget

import com.acme.transport.kit.text.normalize

/** A rule a rolling defect target must pass. Check returns its problems, or none. */
interface RollingDefectTargetRule {
    fun check(item: RollingDefectTarget): MutableList<String>
}

data class RollingDefectTargetRequiredTitleRule(var message: String) : RollingDefectTargetRule {
    override fun check(item: RollingDefectTarget): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDefectTargetLabelLengthRule(var maxLength: Int) : RollingDefectTargetRule {
    override fun check(item: RollingDefectTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDefectTargetCodeLengthRule(var maxLength: Int) : RollingDefectTargetRule {
    override fun check(item: RollingDefectTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDefectTargetRequiredCodeRule(var message: String) : RollingDefectTargetRule {
    override fun check(item: RollingDefectTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDefectTargetAllowedCountryRule(var allowed: MutableList<String>) : RollingDefectTargetRule {
    override fun check(item: RollingDefectTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.country))) {
            return mutableListOf<String>("country ${item.country} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling defect target. */
data class RollingDefectTargetValidator(var rules: MutableList<RollingDefectTargetRule>) {
    fun validate(item: RollingDefectTarget): MutableList<String> {
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

    fun requireValid(item: RollingDefectTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDefectTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDefectTargetValidator(): RollingDefectTargetValidator {
    return RollingDefectTargetValidator(
        mutableListOf<RollingDefectTargetRule>(
            RollingDefectTargetRequiredTitleRule("rolling defect target needs a value"),
            RollingDefectTargetLabelLengthRule(32),
            RollingDefectTargetCodeLengthRule(16),
            RollingDefectTargetRequiredCodeRule("rolling defect target needs an entry"),
            RollingDefectTargetAllowedCountryRule(mutableListOf<String>()),
        ),
    )
}
