package com.acme.transport.lastmile.locker.cycle

/** A rule a locker template must pass. Check returns its problems, or none. */
interface LockerTemplateRule {
    fun check(item: LockerTemplate): MutableList<String>
}

data class LockerTemplateCodePrefixRule(var prefix: String) : LockerTemplateRule {
    override fun check(item: LockerTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LockerTemplateRequiredShiftCodeRule(var message: String) : LockerTemplateRule {
    override fun check(item: LockerTemplate): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LockerTemplateQuantityRangeRule(var min: Int, var max: Int) : LockerTemplateRule {
    override fun check(item: LockerTemplate): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LockerTemplateTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : LockerTemplateRule {
    override fun check(item: LockerTemplate): MutableList<String> {
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

/** Runs every rule of a locker template. */
data class LockerTemplateValidator(var rules: MutableList<LockerTemplateRule>) {
    fun validate(item: LockerTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LockerTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LockerTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultLockerTemplateValidator(): LockerTemplateValidator {
    return LockerTemplateValidator(
        mutableListOf<LockerTemplateRule>(
            LockerTemplateCodePrefixRule("OPS"),
            LockerTemplateRequiredShiftCodeRule("locker template needs an entry"),
            LockerTemplateQuantityRangeRule(64, 1000),
            LockerTemplateTagsLimitRule(86400, mutableListOf<String>("regional", "legacy")),
        ),
    )
}
