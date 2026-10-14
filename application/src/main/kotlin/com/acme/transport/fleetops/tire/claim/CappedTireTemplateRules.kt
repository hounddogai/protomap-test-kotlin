package com.acme.transport.fleetops.tire.claim

/** A rule a capped tire template must pass. Check returns its problems, or none. */
interface CappedTireTemplateRule {
    fun check(item: CappedTireTemplate): MutableList<String>
}

data class CappedTireTemplateRequiredLabelRule(var message: String) : CappedTireTemplateRule {
    override fun check(item: CappedTireTemplate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTireTemplateCodeLengthRule(var maxLength: Int) : CappedTireTemplateRule {
    override fun check(item: CappedTireTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedTireTemplateRequiredCodeRule(var message: String) : CappedTireTemplateRule {
    override fun check(item: CappedTireTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped tire template. */
data class CappedTireTemplateValidator(var rules: MutableList<CappedTireTemplateRule>) {
    fun validate(item: CappedTireTemplate): MutableList<String> {
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

    fun requireValid(item: CappedTireTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTireTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTireTemplateValidator(): CappedTireTemplateValidator {
    return CappedTireTemplateValidator(
        mutableListOf<CappedTireTemplateRule>(
            CappedTireTemplateRequiredLabelRule("capped tire template needs a value"),
            CappedTireTemplateCodeLengthRule(3600),
            CappedTireTemplateRequiredCodeRule("capped tire template needs a value"),
        ),
    )
}
