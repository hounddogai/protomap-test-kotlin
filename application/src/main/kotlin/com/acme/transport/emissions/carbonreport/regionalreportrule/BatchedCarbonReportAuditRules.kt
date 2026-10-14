package com.acme.transport.emissions.carbonreport.regionalreportrule

/** A rule a batched carbon report audit must pass. Check returns its problems, or none. */
interface BatchedCarbonReportAuditRule {
    fun check(item: BatchedCarbonReportAudit): MutableList<String>
}

data class BatchedCarbonReportAuditCodePrefixRule(var prefix: String) : BatchedCarbonReportAuditRule {
    override fun check(item: BatchedCarbonReportAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCarbonReportAuditExternalRefLengthRule(var maxLength: Int) : BatchedCarbonReportAuditRule {
    override fun check(item: BatchedCarbonReportAudit): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCarbonReportAuditLimitRangeRule(var min: Int, var max: Int) : BatchedCarbonReportAuditRule {
    override fun check(item: BatchedCarbonReportAudit): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedCarbonReportAuditRequiredExternalRefRule(var message: String) : BatchedCarbonReportAuditRule {
    override fun check(item: BatchedCarbonReportAudit): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched carbon report audit. */
data class BatchedCarbonReportAuditValidator(var rules: MutableList<BatchedCarbonReportAuditRule>) {
    fun validate(item: BatchedCarbonReportAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedCarbonReportAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedCarbonReportAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedCarbonReportAuditValidator(): BatchedCarbonReportAuditValidator {
    return BatchedCarbonReportAuditValidator(
        mutableListOf<BatchedCarbonReportAuditRule>(
            BatchedCarbonReportAuditCodePrefixRule("OPS"),
            BatchedCarbonReportAuditExternalRefLengthRule(120),
            BatchedCarbonReportAuditLimitRangeRule(32, 120),
            BatchedCarbonReportAuditRequiredExternalRefRule("batched carbon report audit needs an entry"),
        ),
    )
}
