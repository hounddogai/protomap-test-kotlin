package com.acme.transport.network.cutoff.recorditem

/** A rule a capped cutoff target must pass. Check returns its problems, or none. */
interface CappedCutoffTargetRule {
    fun check(item: CappedCutoffTarget): MutableList<String>
}

data class CappedCutoffTargetCodeLengthRule(var maxLength: Int) : CappedCutoffTargetRule {
    override fun check(item: CappedCutoffTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedCutoffTargetMinimumRangeRule(var min: Double, var max: Double) : CappedCutoffTargetRule {
    override fun check(item: CappedCutoffTarget): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class CappedCutoffTargetTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : CappedCutoffTargetRule {
    override fun check(item: CappedCutoffTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class CappedCutoffTargetRequiredLabelRule(var message: String) : CappedCutoffTargetRule {
    override fun check(item: CappedCutoffTarget): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedCutoffTargetDurationMinutesRangeRule(var min: Int, var max: Int) : CappedCutoffTargetRule {
    override fun check(item: CappedCutoffTarget): MutableList<String> {
        val value = item.durationMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("duration minutes $value is below $min")
        }
        if (value > max) {
            problems.add("duration minutes $value is above $max")
        }
        return problems
    }
}

data class CappedCutoffTargetCostRangeRule(var min: Double, var max: Double) : CappedCutoffTargetRule {
    override fun check(item: CappedCutoffTarget): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a capped cutoff target. */
data class CappedCutoffTargetValidator(var rules: MutableList<CappedCutoffTargetRule>) {
    fun validate(item: CappedCutoffTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedCutoffTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedCutoffTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedCutoffTargetValidator(): CappedCutoffTargetValidator {
    return CappedCutoffTargetValidator(
        mutableListOf<CappedCutoffTargetRule>(
            CappedCutoffTargetCodeLengthRule(255),
            CappedCutoffTargetMinimumRangeRule(9.5, 4961.6),
            CappedCutoffTargetTagsLimitRule(120, mutableListOf<String>("legacy", "rolling", "strict")),
            CappedCutoffTargetRequiredLabelRule("capped cutoff target needs a value"),
            CappedCutoffTargetDurationMinutesRangeRule(16, 64),
            CappedCutoffTargetCostRangeRule(2.4, 2758.9),
        ),
    )
}
