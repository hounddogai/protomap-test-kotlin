package com.acme.transport.emissions.carbonreport.reportmapping

/** A rule a primary carbon report template must pass. Check returns its problems, or none. */
interface PrimaryCarbonReportTemplateRule {
    fun check(item: PrimaryCarbonReportTemplate): MutableList<String>
}

data class PrimaryCarbonReportTemplateDiscountRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryCarbonReportTemplateRule {
    override fun check(item: PrimaryCarbonReportTemplate): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCarbonReportTemplateRequiredCodeRule(var message: String) : PrimaryCarbonReportTemplateRule {
    override fun check(item: PrimaryCarbonReportTemplate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCarbonReportTemplateCodePrefixRule(var prefix: String) : PrimaryCarbonReportTemplateRule {
    override fun check(item: PrimaryCarbonReportTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCarbonReportTemplateCodeLengthRule(var maxLength: Int) : PrimaryCarbonReportTemplateRule {
    override fun check(item: PrimaryCarbonReportTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCarbonReportTemplateSummaryLengthRule(var maxLength: Int) : PrimaryCarbonReportTemplateRule {
    override fun check(item: PrimaryCarbonReportTemplate): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary carbon report template. */
data class PrimaryCarbonReportTemplateValidator(var rules: MutableList<PrimaryCarbonReportTemplateRule>) {
    fun validate(item: PrimaryCarbonReportTemplate): MutableList<String> {
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

    fun requireValid(item: PrimaryCarbonReportTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCarbonReportTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCarbonReportTemplateValidator(): PrimaryCarbonReportTemplateValidator {
    return PrimaryCarbonReportTemplateValidator(
        mutableListOf<PrimaryCarbonReportTemplateRule>(
            PrimaryCarbonReportTemplateDiscountRangeRule(3.3, 1316.1),
            PrimaryCarbonReportTemplateRequiredCodeRule("primary carbon report template needs an entry"),
            PrimaryCarbonReportTemplateCodePrefixRule("REF"),
            PrimaryCarbonReportTemplateCodeLengthRule(255),
            PrimaryCarbonReportTemplateSummaryLengthRule(64),
        ),
    )
}
