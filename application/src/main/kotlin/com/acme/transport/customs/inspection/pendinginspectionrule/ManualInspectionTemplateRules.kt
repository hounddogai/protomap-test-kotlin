package com.acme.transport.customs.inspection.pendinginspectionrule

/** A rule a manual inspection template must pass. Check returns its problems, or none. */
interface ManualInspectionTemplateRule {
    fun check(item: ManualInspectionTemplate): MutableList<String>
}

data class ManualInspectionTemplateRequiredOwnerIdRule(var message: String) : ManualInspectionTemplateRule {
    override fun check(item: ManualInspectionTemplate): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionTemplateCodePrefixRule(var prefix: String) : ManualInspectionTemplateRule {
    override fun check(item: ManualInspectionTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionTemplateOwnerIdLengthRule(var maxLength: Int) : ManualInspectionTemplateRule {
    override fun check(item: ManualInspectionTemplate): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionTemplateCountryLengthRule(var maxLength: Int) : ManualInspectionTemplateRule {
    override fun check(item: ManualInspectionTemplate): MutableList<String> {
        val size = item.country.length
        if (size > maxLength) {
            return mutableListOf<String>("country is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualInspectionTemplateRequiredCodeRule(var message: String) : ManualInspectionTemplateRule {
    override fun check(item: ManualInspectionTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual inspection template. */
data class ManualInspectionTemplateValidator(var rules: MutableList<ManualInspectionTemplateRule>) {
    fun validate(item: ManualInspectionTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualInspectionTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualInspectionTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultManualInspectionTemplateValidator(): ManualInspectionTemplateValidator {
    return ManualInspectionTemplateValidator(
        mutableListOf<ManualInspectionTemplateRule>(
            ManualInspectionTemplateRequiredOwnerIdRule("manual inspection template needs a value"),
            ManualInspectionTemplateCodePrefixRule("REF"),
            ManualInspectionTemplateOwnerIdLengthRule(86400),
            ManualInspectionTemplateCountryLengthRule(255),
            ManualInspectionTemplateRequiredCodeRule("manual inspection template needs an entry"),
        ),
    )
}
