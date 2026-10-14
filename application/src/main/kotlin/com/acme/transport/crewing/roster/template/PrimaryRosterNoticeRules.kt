package com.acme.transport.crewing.roster.template

/** A rule a primary roster notice must pass. Check returns its problems, or none. */
interface PrimaryRosterNoticeRule {
    fun check(item: PrimaryRosterNotice): MutableList<String>
}

data class PrimaryRosterNoticeRequiredLabelRule(var message: String) : PrimaryRosterNoticeRule {
    override fun check(item: PrimaryRosterNotice): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterNoticeLabelLengthRule(var maxLength: Int) : PrimaryRosterNoticeRule {
    override fun check(item: PrimaryRosterNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterNoticeRatioRangeRule(var min: Double, var max: Double) : PrimaryRosterNoticeRule {
    override fun check(item: PrimaryRosterNotice): MutableList<String> {
        val value = item.ratio
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ratio $value is below $min")
        }
        if (value > max) {
            problems.add("ratio $value is above $max")
        }
        return problems
    }
}

data class PrimaryRosterNoticeScoreRangeRule(var min: Double, var max: Double) : PrimaryRosterNoticeRule {
    override fun check(item: PrimaryRosterNotice): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterNoticeCodePrefixRule(var prefix: String) : PrimaryRosterNoticeRule {
    override fun check(item: PrimaryRosterNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryRosterNoticeCreatedAtRangeRule(var min: Int, var max: Int) : PrimaryRosterNoticeRule {
    override fun check(item: PrimaryRosterNotice): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary roster notice. */
data class PrimaryRosterNoticeValidator(var rules: MutableList<PrimaryRosterNoticeRule>) {
    fun validate(item: PrimaryRosterNotice): MutableList<String> {
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

    fun requireValid(item: PrimaryRosterNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryRosterNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryRosterNoticeValidator(): PrimaryRosterNoticeValidator {
    return PrimaryRosterNoticeValidator(
        mutableListOf<PrimaryRosterNoticeRule>(
            PrimaryRosterNoticeRequiredLabelRule("primary roster notice needs an entry"),
            PrimaryRosterNoticeLabelLengthRule(255),
            PrimaryRosterNoticeRatioRangeRule(0.8, 1721.7),
            PrimaryRosterNoticeScoreRangeRule(7.0, 1697.3),
            PrimaryRosterNoticeCodePrefixRule("ACM"),
            PrimaryRosterNoticeCreatedAtRangeRule(16, 32),
        ),
    )
}
