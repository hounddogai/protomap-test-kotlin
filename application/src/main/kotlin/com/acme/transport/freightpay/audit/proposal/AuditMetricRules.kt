package com.acme.transport.freightpay.audit.proposal

/** A rule a audit metric must pass. Check returns its problems, or none. */
interface AuditMetricRule {
    fun check(item: AuditMetric): MutableList<String>
}

data class AuditMetricTitleLengthRule(var maxLength: Int) : AuditMetricRule {
    override fun check(item: AuditMetric): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditMetricRequiredShiftCodeRule(var message: String) : AuditMetricRule {
    override fun check(item: AuditMetric): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditMetricStartsAtRangeRule(var min: Int, var max: Int) : AuditMetricRule {
    override fun check(item: AuditMetric): MutableList<String> {
        val value = item.startsAt
        if (value < min || value > max) {
            return mutableListOf<String>("starts at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AuditMetricCodeLengthRule(var maxLength: Int) : AuditMetricRule {
    override fun check(item: AuditMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditMetricRequiredDriverIdRule(var message: String) : AuditMetricRule {
    override fun check(item: AuditMetric): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditMetricRequiredTitleRule(var message: String) : AuditMetricRule {
    override fun check(item: AuditMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit metric. */
data class AuditMetricValidator(var rules: MutableList<AuditMetricRule>) {
    fun validate(item: AuditMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditMetricValidator(): AuditMetricValidator {
    return AuditMetricValidator(
        mutableListOf<AuditMetricRule>(
            AuditMetricTitleLengthRule(1000),
            AuditMetricRequiredShiftCodeRule("audit metric needs this field"),
            AuditMetricStartsAtRangeRule(120, 86400),
            AuditMetricCodeLengthRule(255),
            AuditMetricRequiredDriverIdRule("audit metric needs a value"),
            AuditMetricRequiredTitleRule("audit metric needs a value"),
        ),
    )
}
