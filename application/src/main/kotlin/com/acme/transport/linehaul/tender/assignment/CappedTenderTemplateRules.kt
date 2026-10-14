package com.acme.transport.linehaul.tender.assignment

/** A rule a capped tender template must pass. Check returns its problems, or none. */
interface CappedTenderTemplateRule {
    fun check(item: CappedTenderTemplate): MutableList<String>
}

data class CappedTenderTemplateLabelLengthRule(var maxLength: Int) : CappedTenderTemplateRule {
    override fun check(item: CappedTenderTemplate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTenderTemplateRequiredOwnerIdRule(var message: String) : CappedTenderTemplateRule {
    override fun check(item: CappedTenderTemplate): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTenderTemplateOwnerIdLengthRule(var maxLength: Int) : CappedTenderTemplateRule {
    override fun check(item: CappedTenderTemplate): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTenderTemplateRequiredCodeRule(var message: String) : CappedTenderTemplateRule {
    override fun check(item: CappedTenderTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTenderTemplateRequiredLabelRule(var message: String) : CappedTenderTemplateRule {
    override fun check(item: CappedTenderTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped tender template. */
data class CappedTenderTemplateValidator(var rules: MutableList<CappedTenderTemplateRule>) {
    fun validate(item: CappedTenderTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedTenderTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTenderTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTenderTemplateValidator(): CappedTenderTemplateValidator {
    return CappedTenderTemplateValidator(
        mutableListOf<CappedTenderTemplateRule>(
            CappedTenderTemplateLabelLengthRule(86400),
            CappedTenderTemplateRequiredOwnerIdRule("capped tender template needs this field"),
            CappedTenderTemplateOwnerIdLengthRule(16),
            CappedTenderTemplateRequiredCodeRule("capped tender template needs this field"),
            CappedTenderTemplateRequiredLabelRule("capped tender template needs a value"),
        ),
    )
}
