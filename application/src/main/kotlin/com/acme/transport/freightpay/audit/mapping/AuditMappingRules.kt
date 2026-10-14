package com.acme.transport.freightpay.audit.mapping

/** A rule a audit mapping must pass. Check returns its problems, or none. */
interface AuditMappingRule {
    fun check(item: AuditMapping): MutableList<String>
}

data class AuditMappingRequiredDriverIdRule(var message: String) : AuditMappingRule {
    override fun check(item: AuditMapping): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AuditMappingReviewerLengthRule(var maxLength: Int) : AuditMappingRule {
    override fun check(item: AuditMapping): MutableList<String> {
        val size = item.reviewer.length
        if (size > maxLength) {
            return mutableListOf<String>("reviewer is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditMappingFuelLitersRangeRule(var min: Double, var max: Double) : AuditMappingRule {
    override fun check(item: AuditMapping): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a audit mapping. */
data class AuditMappingValidator(var rules: MutableList<AuditMappingRule>) {
    fun validate(item: AuditMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AuditMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditMappingValidator(): AuditMappingValidator {
    return AuditMappingValidator(
        mutableListOf<AuditMappingRule>(
            AuditMappingRequiredDriverIdRule("audit mapping needs this field"),
            AuditMappingReviewerLengthRule(80),
            AuditMappingFuelLitersRangeRule(5.6, 1456.6),
        ),
    )
}
