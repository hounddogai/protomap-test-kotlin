package com.acme.transport.linehaul.backhaul.template

/** A rule a backhaul template must pass. Check returns its problems, or none. */
interface BackhaulTemplateRule {
    fun check(item: BackhaulTemplate): MutableList<String>
}

data class BackhaulTemplateNameLengthRule(var maxLength: Int) : BackhaulTemplateRule {
    override fun check(item: BackhaulTemplate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulTemplateCeilingRangeRule(var min: Double, var max: Double) : BackhaulTemplateRule {
    override fun check(item: BackhaulTemplate): MutableList<String> {
        val value = item.ceiling
        if (value < min || value > max) {
            return mutableListOf<String>("ceiling $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulTemplateOwnerIdLengthRule(var maxLength: Int) : BackhaulTemplateRule {
    override fun check(item: BackhaulTemplate): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulTemplateCodeLengthRule(var maxLength: Int) : BackhaulTemplateRule {
    override fun check(item: BackhaulTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul template. */
data class BackhaulTemplateValidator(var rules: MutableList<BackhaulTemplateRule>) {
    fun validate(item: BackhaulTemplate): MutableList<String> {
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

    fun requireValid(item: BackhaulTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulTemplateValidator(): BackhaulTemplateValidator {
    return BackhaulTemplateValidator(
        mutableListOf<BackhaulTemplateRule>(
            BackhaulTemplateNameLengthRule(64),
            BackhaulTemplateCeilingRangeRule(8.2, 800.4),
            BackhaulTemplateOwnerIdLengthRule(16),
            BackhaulTemplateCodeLengthRule(3600),
        ),
    )
}
