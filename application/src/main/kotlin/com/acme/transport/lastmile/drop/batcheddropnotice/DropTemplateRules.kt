package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.kit.text.normalize

/** A rule a drop template must pass. Check returns its problems, or none. */
interface DropTemplateRule {
    fun check(item: DropTemplate): MutableList<String>
}

data class DropTemplateRequiredNotesRule(var message: String) : DropTemplateRule {
    override fun check(item: DropTemplate): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropTemplateArchivedGuardRule(var limit: Double) : DropTemplateRule {
    override fun check(item: DropTemplate): MutableList<String> {
        if (item.archived && item.maximum > limit) {
            return mutableListOf<String>("maximum ${item.maximum} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class DropTemplateNotesLengthRule(var maxLength: Int) : DropTemplateRule {
    override fun check(item: DropTemplate): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropTemplateAllowedLabelRule(var allowed: MutableList<String>) : DropTemplateRule {
    override fun check(item: DropTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is inconsistent")
        }
        return mutableListOf<String>()
    }
}

data class DropTemplateRequiredLabelRule(var message: String) : DropTemplateRule {
    override fun check(item: DropTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop template. */
data class DropTemplateValidator(var rules: MutableList<DropTemplateRule>) {
    fun validate(item: DropTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DropTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultDropTemplateValidator(): DropTemplateValidator {
    return DropTemplateValidator(
        mutableListOf<DropTemplateRule>(
            DropTemplateRequiredNotesRule("drop template needs an entry"),
            DropTemplateArchivedGuardRule(8.5),
            DropTemplateNotesLengthRule(64),
            DropTemplateAllowedLabelRule(mutableListOf<String>()),
            DropTemplateRequiredLabelRule("drop template needs a value"),
        ),
    )
}
