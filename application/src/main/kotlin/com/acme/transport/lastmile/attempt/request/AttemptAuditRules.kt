package com.acme.transport.lastmile.attempt.request

/** A rule a attempt audit must pass. Check returns its problems, or none. */
interface AttemptAuditRule {
    fun check(item: AttemptAudit): MutableList<String>
}

data class AttemptAuditTimezoneLengthRule(var maxLength: Int) : AttemptAuditRule {
    override fun check(item: AttemptAudit): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AttemptAuditFuelLitersRangeRule(var min: Double, var max: Double) : AttemptAuditRule {
    override fun check(item: AttemptAudit): MutableList<String> {
        val value = item.fuelLiters
        if (value < min || value > max) {
            return mutableListOf<String>("fuel liters $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class AttemptAuditPalletsRangeRule(var min: Int, var max: Int) : AttemptAuditRule {
    override fun check(item: AttemptAudit): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a attempt audit. */
data class AttemptAuditValidator(var rules: MutableList<AttemptAuditRule>) {
    fun validate(item: AttemptAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: AttemptAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AttemptAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultAttemptAuditValidator(): AttemptAuditValidator {
    return AttemptAuditValidator(
        mutableListOf<AttemptAuditRule>(
            AttemptAuditTimezoneLengthRule(80),
            AttemptAuditFuelLitersRangeRule(4.0, 432.3),
            AttemptAuditPalletsRangeRule(120, 3600),
        ),
    )
}
