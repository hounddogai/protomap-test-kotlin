package com.acme.transport.customs.declaration.plan

/** A rule a regional declaration template must pass. Check returns its problems, or none. */
interface RegionalDeclarationTemplateRule {
    fun check(item: RegionalDeclarationTemplate): MutableList<String>
}

data class RegionalDeclarationTemplateRevisionRangeRule(
    var min: Int,
    var max: Int,
) : RegionalDeclarationTemplateRule {
    override fun check(item: RegionalDeclarationTemplate): MutableList<String> {
        val value = item.revision
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("revision $value is below $min")
        }
        if (value > max) {
            problems.add("revision $value is above $max")
        }
        return problems
    }
}

data class RegionalDeclarationTemplateCapacityRangeRule(
    var min: Int,
    var max: Int,
) : RegionalDeclarationTemplateRule {
    override fun check(item: RegionalDeclarationTemplate): MutableList<String> {
        val value = item.capacity
        if (value < min || value > max) {
            return mutableListOf<String>("capacity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDeclarationTemplateCodeLengthRule(var maxLength: Int) : RegionalDeclarationTemplateRule {
    override fun check(item: RegionalDeclarationTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDeclarationTemplateLabelLengthRule(var maxLength: Int) : RegionalDeclarationTemplateRule {
    override fun check(item: RegionalDeclarationTemplate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional declaration template. */
data class RegionalDeclarationTemplateValidator(var rules: MutableList<RegionalDeclarationTemplateRule>) {
    fun validate(item: RegionalDeclarationTemplate): MutableList<String> {
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

    fun requireValid(item: RegionalDeclarationTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDeclarationTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDeclarationTemplateValidator(): RegionalDeclarationTemplateValidator {
    return RegionalDeclarationTemplateValidator(
        mutableListOf<RegionalDeclarationTemplateRule>(
            RegionalDeclarationTemplateRevisionRangeRule(64, 1000),
            RegionalDeclarationTemplateCapacityRangeRule(80, 3600),
            RegionalDeclarationTemplateCodeLengthRule(86400),
            RegionalDeclarationTemplateLabelLengthRule(32),
        ),
    )
}
