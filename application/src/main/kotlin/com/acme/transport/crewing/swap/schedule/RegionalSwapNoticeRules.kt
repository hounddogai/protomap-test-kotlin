package com.acme.transport.crewing.swap.schedule

/** A rule a regional swap notice must pass. Check returns its problems, or none. */
interface RegionalSwapNoticeRule {
    fun check(item: RegionalSwapNotice): MutableList<String>
}

data class RegionalSwapNoticeNotesLengthRule(var maxLength: Int) : RegionalSwapNoticeRule {
    override fun check(item: RegionalSwapNotice): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSwapNoticeOwnerIdLengthRule(var maxLength: Int) : RegionalSwapNoticeRule {
    override fun check(item: RegionalSwapNotice): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSwapNoticeRequiredOwnerIdRule(var message: String) : RegionalSwapNoticeRule {
    override fun check(item: RegionalSwapNotice): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalSwapNoticeCodePrefixRule(var prefix: String) : RegionalSwapNoticeRule {
    override fun check(item: RegionalSwapNotice): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSwapNoticeMarginRangeRule(var min: Double, var max: Double) : RegionalSwapNoticeRule {
    override fun check(item: RegionalSwapNotice): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalSwapNoticeLabelLengthRule(var maxLength: Int) : RegionalSwapNoticeRule {
    override fun check(item: RegionalSwapNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional swap notice. */
data class RegionalSwapNoticeValidator(var rules: MutableList<RegionalSwapNoticeRule>) {
    fun validate(item: RegionalSwapNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalSwapNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalSwapNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalSwapNoticeValidator(): RegionalSwapNoticeValidator {
    return RegionalSwapNoticeValidator(
        mutableListOf<RegionalSwapNoticeRule>(
            RegionalSwapNoticeNotesLengthRule(16),
            RegionalSwapNoticeOwnerIdLengthRule(255),
            RegionalSwapNoticeRequiredOwnerIdRule("regional swap notice needs this field"),
            RegionalSwapNoticeCodePrefixRule("X"),
            RegionalSwapNoticeMarginRangeRule(9.2, 2524.7),
            RegionalSwapNoticeLabelLengthRule(80),
        ),
    )
}
