package com.acme.transport.terminal.dockdoor.template

/** A rule a capped dock door template must pass. Check returns its problems, or none. */
interface CappedDockDoorTemplateRule {
    fun check(item: CappedDockDoorTemplate): MutableList<String>
}

data class CappedDockDoorTemplateArchivedGuardRule(var limit: Double) : CappedDockDoorTemplateRule {
    override fun check(item: CappedDockDoorTemplate): MutableList<String> {
        if (item.archived && item.threshold > limit) {
            return mutableListOf<String>("threshold ${item.threshold} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorTemplateCodeLengthRule(var maxLength: Int) : CappedDockDoorTemplateRule {
    override fun check(item: CappedDockDoorTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorTemplateRequiredLabelRule(var message: String) : CappedDockDoorTemplateRule {
    override fun check(item: CappedDockDoorTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorTemplateRequiredContactEmailRule(var message: String) : CappedDockDoorTemplateRule {
    override fun check(item: CappedDockDoorTemplate): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorTemplateLabelLengthRule(var maxLength: Int) : CappedDockDoorTemplateRule {
    override fun check(item: CappedDockDoorTemplate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedDockDoorTemplateRequiredNameRule(var message: String) : CappedDockDoorTemplateRule {
    override fun check(item: CappedDockDoorTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped dock door template. */
data class CappedDockDoorTemplateValidator(var rules: MutableList<CappedDockDoorTemplateRule>) {
    fun validate(item: CappedDockDoorTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedDockDoorTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedDockDoorTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedDockDoorTemplateValidator(): CappedDockDoorTemplateValidator {
    return CappedDockDoorTemplateValidator(
        mutableListOf<CappedDockDoorTemplateRule>(
            CappedDockDoorTemplateArchivedGuardRule(2.9),
            CappedDockDoorTemplateCodeLengthRule(120),
            CappedDockDoorTemplateRequiredLabelRule("capped dock door template needs this field"),
            CappedDockDoorTemplateRequiredContactEmailRule("capped dock door template needs this field"),
            CappedDockDoorTemplateLabelLengthRule(255),
            CappedDockDoorTemplateRequiredNameRule("capped dock door template needs an entry"),
        ),
    )
}
