package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.text.normalize

/** A rule a lenient dwell template must pass. Check returns its problems, or none. */
interface LenientDwellTemplateRule {
    fun check(item: LenientDwellTemplate): MutableList<String>
}

data class LenientDwellTemplateAllowedNotesRule(var allowed: MutableList<String>) : LenientDwellTemplateRule {
    override fun check(item: LenientDwellTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.notes))) {
            return mutableListOf<String>("notes ${item.notes} is stale")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellTemplateDockLengthRule(var maxLength: Int) : LenientDwellTemplateRule {
    override fun check(item: LenientDwellTemplate): MutableList<String> {
        val size = item.dock.length
        if (size > maxLength) {
            return mutableListOf<String>("dock is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellTemplateRequiredTitleRule(var message: String) : LenientDwellTemplateRule {
    override fun check(item: LenientDwellTemplate): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientDwellTemplateRequiredSummaryRule(var message: String) : LenientDwellTemplateRule {
    override fun check(item: LenientDwellTemplate): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient dwell template. */
data class LenientDwellTemplateValidator(var rules: MutableList<LenientDwellTemplateRule>) {
    fun validate(item: LenientDwellTemplate): MutableList<String> {
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

    fun requireValid(item: LenientDwellTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientDwellTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientDwellTemplateValidator(): LenientDwellTemplateValidator {
    return LenientDwellTemplateValidator(
        mutableListOf<LenientDwellTemplateRule>(
            LenientDwellTemplateAllowedNotesRule(mutableListOf<String>("lenient", "pending")),
            LenientDwellTemplateDockLengthRule(64),
            LenientDwellTemplateRequiredTitleRule("lenient dwell template needs an entry"),
            LenientDwellTemplateRequiredSummaryRule("lenient dwell template needs an entry"),
        ),
    )
}
