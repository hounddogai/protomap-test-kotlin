package com.acme.transport.terminal.dwell.review

/** A rule a scheduled dwell template must pass. Check returns its problems, or none. */
interface ScheduledDwellTemplateRule {
    fun check(item: ScheduledDwellTemplate): MutableList<String>
}

data class ScheduledDwellTemplateChecksumLengthRule(var maxLength: Int) : ScheduledDwellTemplateRule {
    override fun check(item: ScheduledDwellTemplate): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDwellTemplateTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : ScheduledDwellTemplateRule {
    override fun check(item: ScheduledDwellTemplate): MutableList<String> {
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

data class ScheduledDwellTemplateTitleLengthRule(var maxLength: Int) : ScheduledDwellTemplateRule {
    override fun check(item: ScheduledDwellTemplate): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDwellTemplateBudgetRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledDwellTemplateRule {
    override fun check(item: ScheduledDwellTemplate): MutableList<String> {
        val value = item.budget
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("budget $value is below $min")
        }
        if (value > max) {
            problems.add("budget $value is above $max")
        }
        return problems
    }
}

data class ScheduledDwellTemplateFuelLitersRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledDwellTemplateRule {
    override fun check(item: ScheduledDwellTemplate): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDwellTemplateShiftCodeLengthRule(var maxLength: Int) : ScheduledDwellTemplateRule {
    override fun check(item: ScheduledDwellTemplate): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled dwell template. */
data class ScheduledDwellTemplateValidator(var rules: MutableList<ScheduledDwellTemplateRule>) {
    fun validate(item: ScheduledDwellTemplate): MutableList<String> {
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

    fun requireValid(item: ScheduledDwellTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDwellTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDwellTemplateValidator(): ScheduledDwellTemplateValidator {
    return ScheduledDwellTemplateValidator(
        mutableListOf<ScheduledDwellTemplateRule>(
            ScheduledDwellTemplateChecksumLengthRule(80),
            ScheduledDwellTemplateTagsLimitRule(1000, mutableListOf<String>("capped")),
            ScheduledDwellTemplateTitleLengthRule(64),
            ScheduledDwellTemplateBudgetRangeRule(5.5, 752.9),
            ScheduledDwellTemplateFuelLitersRangeRule(6.1, 4696.6),
            ScheduledDwellTemplateShiftCodeLengthRule(86400),
        ),
    )
}
