package com.acme.transport.linehaul.backhaul.rollup

/** A rule a primary backhaul score must pass. Check returns its problems, or none. */
interface PrimaryBackhaulScoreRule {
    fun check(item: PrimaryBackhaulScore): MutableList<String>
}

data class PrimaryBackhaulScoreRequiredCodeRule(var message: String) : PrimaryBackhaulScoreRule {
    override fun check(item: PrimaryBackhaulScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulScoreLabelLengthRule(var maxLength: Int) : PrimaryBackhaulScoreRule {
    override fun check(item: PrimaryBackhaulScore): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryBackhaulScorePalletsRangeRule(var min: Int, var max: Int) : PrimaryBackhaulScoreRule {
    override fun check(item: PrimaryBackhaulScore): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class PrimaryBackhaulScoreRequiredCategoryRule(var message: String) : PrimaryBackhaulScoreRule {
    override fun check(item: PrimaryBackhaulScore): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary backhaul score. */
data class PrimaryBackhaulScoreValidator(var rules: MutableList<PrimaryBackhaulScoreRule>) {
    fun validate(item: PrimaryBackhaulScore): MutableList<String> {
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

    fun requireValid(item: PrimaryBackhaulScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryBackhaulScore>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryBackhaulScoreValidator(): PrimaryBackhaulScoreValidator {
    return PrimaryBackhaulScoreValidator(
        mutableListOf<PrimaryBackhaulScoreRule>(
            PrimaryBackhaulScoreRequiredCodeRule("primary backhaul score needs this field"),
            PrimaryBackhaulScoreLabelLengthRule(16),
            PrimaryBackhaulScorePalletsRangeRule(80, 86400),
            PrimaryBackhaulScoreRequiredCategoryRule("primary backhaul score needs an entry"),
        ),
    )
}
