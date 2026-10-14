package com.acme.transport.customs.declaration.milestone

/** A rule a strict declaration milestone must pass. Check returns its problems, or none. */
interface StrictDeclarationMilestoneRule {
    fun check(item: StrictDeclarationMilestone): MutableList<String>
}

data class StrictDeclarationMilestoneCodePrefixRule(var prefix: String) : StrictDeclarationMilestoneRule {
    override fun check(item: StrictDeclarationMilestone): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictDeclarationMilestoneLabelLengthRule(var maxLength: Int) : StrictDeclarationMilestoneRule {
    override fun check(item: StrictDeclarationMilestone): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDeclarationMilestoneCodeLengthRule(var maxLength: Int) : StrictDeclarationMilestoneRule {
    override fun check(item: StrictDeclarationMilestone): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDeclarationMilestoneNameLengthRule(var maxLength: Int) : StrictDeclarationMilestoneRule {
    override fun check(item: StrictDeclarationMilestone): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictDeclarationMilestoneRequiredNameRule(var message: String) : StrictDeclarationMilestoneRule {
    override fun check(item: StrictDeclarationMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictDeclarationMilestoneStartsAtRangeRule(
    var min: Int,
    var max: Int,
) : StrictDeclarationMilestoneRule {
    override fun check(item: StrictDeclarationMilestone): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a strict declaration milestone. */
data class StrictDeclarationMilestoneValidator(var rules: MutableList<StrictDeclarationMilestoneRule>) {
    fun validate(item: StrictDeclarationMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictDeclarationMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictDeclarationMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictDeclarationMilestoneValidator(): StrictDeclarationMilestoneValidator {
    return StrictDeclarationMilestoneValidator(
        mutableListOf<StrictDeclarationMilestoneRule>(
            StrictDeclarationMilestoneCodePrefixRule("Q"),
            StrictDeclarationMilestoneLabelLengthRule(32),
            StrictDeclarationMilestoneCodeLengthRule(3600),
            StrictDeclarationMilestoneNameLengthRule(1000),
            StrictDeclarationMilestoneRequiredNameRule("strict declaration milestone needs this field"),
            StrictDeclarationMilestoneStartsAtRangeRule(120, 3600),
        ),
    )
}
