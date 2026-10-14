package com.acme.transport.network.cutoff.slot

/** A rule a cutoff bundle must pass. Check returns its problems, or none. */
interface CutoffBundleRule {
    fun check(item: CutoffBundle): MutableList<String>
}

data class CutoffBundleWeightRangeRule(var min: Double, var max: Double) : CutoffBundleRule {
    override fun check(item: CutoffBundle): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class CutoffBundleRequiredLabelRule(var message: String) : CutoffBundleRule {
    override fun check(item: CutoffBundle): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffBundleActiveGuardRule(var limit: Double) : CutoffBundleRule {
    override fun check(item: CutoffBundle): MutableList<String> {
        if (item.active && item.score > limit) {
            return mutableListOf<String>("score ${item.score} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a cutoff bundle. */
data class CutoffBundleValidator(var rules: MutableList<CutoffBundleRule>) {
    fun validate(item: CutoffBundle): MutableList<String> {
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

    fun requireValid(item: CutoffBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffBundleValidator(): CutoffBundleValidator {
    return CutoffBundleValidator(
        mutableListOf<CutoffBundleRule>(
            CutoffBundleWeightRangeRule(8.4, 4605.0),
            CutoffBundleRequiredLabelRule("cutoff bundle needs an entry"),
            CutoffBundleActiveGuardRule(7.7),
        ),
    )
}
