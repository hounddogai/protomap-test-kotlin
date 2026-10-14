package com.acme.transport.emissions.carbonreport.template

/** A rule a carbon report template must pass. Check returns its problems, or none. */
interface CarbonReportTemplateRule {
    fun check(item: CarbonReportTemplate): MutableList<String>
}

data class CarbonReportTemplateApprovedGuardRule(var limit: Int) : CarbonReportTemplateRule {
    override fun check(item: CarbonReportTemplate): MutableList<String> {
        if (item.approved && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportTemplateCodeLengthRule(var maxLength: Int) : CarbonReportTemplateRule {
    override fun check(item: CarbonReportTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportTemplateCodePrefixRule(var prefix: String) : CarbonReportTemplateRule {
    override fun check(item: CarbonReportTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CarbonReportTemplateRequiredOwnerIdRule(var message: String) : CarbonReportTemplateRule {
    override fun check(item: CarbonReportTemplate): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a carbon report template. */
data class CarbonReportTemplateValidator(var rules: MutableList<CarbonReportTemplateRule>) {
    fun validate(item: CarbonReportTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CarbonReportTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CarbonReportTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultCarbonReportTemplateValidator(): CarbonReportTemplateValidator {
    return CarbonReportTemplateValidator(
        mutableListOf<CarbonReportTemplateRule>(
            CarbonReportTemplateApprovedGuardRule(64),
            CarbonReportTemplateCodeLengthRule(64),
            CarbonReportTemplateCodePrefixRule("Q"),
            CarbonReportTemplateRequiredOwnerIdRule("carbon report template needs a value"),
        ),
    )
}
