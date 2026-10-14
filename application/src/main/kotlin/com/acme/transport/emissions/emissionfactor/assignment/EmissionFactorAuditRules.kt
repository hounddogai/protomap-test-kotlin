package com.acme.transport.emissions.emissionfactor.assignment

/** A rule a emission factor audit must pass. Check returns its problems, or none. */
interface EmissionFactorAuditRule {
    fun check(item: EmissionFactorAudit): MutableList<String>
}

data class EmissionFactorAuditContactEmailLengthRule(var maxLength: Int) : EmissionFactorAuditRule {
    override fun check(item: EmissionFactorAudit): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAuditRequiredLocaleRule(var message: String) : EmissionFactorAuditRule {
    override fun check(item: EmissionFactorAudit): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAuditRequiredNameRule(var message: String) : EmissionFactorAuditRule {
    override fun check(item: EmissionFactorAudit): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAuditRequiredContactEmailRule(var message: String) : EmissionFactorAuditRule {
    override fun check(item: EmissionFactorAudit): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EmissionFactorAuditStartsAtRangeRule(var min: Int, var max: Int) : EmissionFactorAuditRule {
    override fun check(item: EmissionFactorAudit): MutableList<String> {
        val value = item.startsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("starts at $value is below $min")
        }
        if (value > max) {
            problems.add("starts at $value is above $max")
        }
        return problems
    }
}

data class EmissionFactorAuditCodePrefixRule(var prefix: String) : EmissionFactorAuditRule {
    override fun check(item: EmissionFactorAudit): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a emission factor audit. */
data class EmissionFactorAuditValidator(var rules: MutableList<EmissionFactorAuditRule>) {
    fun validate(item: EmissionFactorAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EmissionFactorAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EmissionFactorAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultEmissionFactorAuditValidator(): EmissionFactorAuditValidator {
    return EmissionFactorAuditValidator(
        mutableListOf<EmissionFactorAuditRule>(
            EmissionFactorAuditContactEmailLengthRule(120),
            EmissionFactorAuditRequiredLocaleRule("emission factor audit needs an entry"),
            EmissionFactorAuditRequiredNameRule("emission factor audit needs this field"),
            EmissionFactorAuditRequiredContactEmailRule("emission factor audit needs this field"),
            EmissionFactorAuditStartsAtRangeRule(120, 3600),
            EmissionFactorAuditCodePrefixRule("OPS"),
        ),
    )
}
