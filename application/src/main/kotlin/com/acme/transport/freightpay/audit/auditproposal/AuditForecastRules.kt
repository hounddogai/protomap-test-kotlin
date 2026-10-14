package com.acme.transport.freightpay.audit.auditproposal

import com.acme.transport.kit.text.normalize

/** A rule a audit forecast must pass. Check returns its problems, or none. */
interface AuditForecastRule {
    fun check(item: AuditForecast): MutableList<String>
}

data class AuditForecastUnitLengthRule(var maxLength: Int) : AuditForecastRule {
    override fun check(item: AuditForecast): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditForecastAllowedRegionRule(var allowed: MutableList<String>) : AuditForecastRule {
    override fun check(item: AuditForecast): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.region))) {
            return mutableListOf<String>("region ${item.region} is out of range")
        }
        return mutableListOf<String>()
    }
}

data class AuditForecastCodeLengthRule(var maxLength: Int) : AuditForecastRule {
    override fun check(item: AuditForecast): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AuditForecastNotesLengthRule(var maxLength: Int) : AuditForecastRule {
    override fun check(item: AuditForecast): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a audit forecast. */
data class AuditForecastValidator(var rules: MutableList<AuditForecastRule>) {
    fun validate(item: AuditForecast): MutableList<String> {
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

    fun requireValid(item: AuditForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AuditForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultAuditForecastValidator(): AuditForecastValidator {
    return AuditForecastValidator(
        mutableListOf<AuditForecastRule>(
            AuditForecastUnitLengthRule(32),
            AuditForecastAllowedRegionRule(mutableListOf<String>()),
            AuditForecastCodeLengthRule(32),
            AuditForecastNotesLengthRule(80),
        ),
    )
}
