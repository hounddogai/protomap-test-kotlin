package com.acme.transport.linehaul.backhaul.variance

import com.acme.transport.kit.text.normalize

/** A rule a backhaul variance must pass. Check returns its problems, or none. */
interface BackhaulVarianceRule {
    fun check(item: BackhaulVariance): MutableList<String>
}

data class BackhaulVarianceTitleLengthRule(var maxLength: Int) : BackhaulVarianceRule {
    override fun check(item: BackhaulVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulVarianceAllowedTitleRule(var allowed: MutableList<String>) : BackhaulVarianceRule {
    override fun check(item: BackhaulVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulVarianceRequiredCodeRule(var message: String) : BackhaulVarianceRule {
    override fun check(item: BackhaulVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulVarianceRequiredCategoryRule(var message: String) : BackhaulVarianceRule {
    override fun check(item: BackhaulVariance): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul variance. */
data class BackhaulVarianceValidator(var rules: MutableList<BackhaulVarianceRule>) {
    fun validate(item: BackhaulVariance): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulVarianceValidator(): BackhaulVarianceValidator {
    return BackhaulVarianceValidator(
        mutableListOf<BackhaulVarianceRule>(
            BackhaulVarianceTitleLengthRule(64),
            BackhaulVarianceAllowedTitleRule(mutableListOf<String>("strict", "global", "regional")),
            BackhaulVarianceRequiredCodeRule("backhaul variance needs an entry"),
            BackhaulVarianceRequiredCategoryRule("backhaul variance needs an entry"),
        ),
    )
}
