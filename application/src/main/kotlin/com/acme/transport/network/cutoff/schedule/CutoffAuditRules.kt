package com.acme.transport.network.cutoff.schedule

/** A rule a cutoff audit must pass. Check returns its problems, or none. */
interface CutoffAuditRule {
    fun check(item: CutoffAudit): MutableList<String>
}

data class CutoffAuditCodeLengthRule(var maxLength: Int) : CutoffAuditRule {
    override fun check(item: CutoffAudit): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffAuditRequiredDepotRule(var message: String) : CutoffAuditRule {
    override fun check(item: CutoffAudit): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CutoffAuditDepotLengthRule(var maxLength: Int) : CutoffAuditRule {
    override fun check(item: CutoffAudit): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CutoffAuditThresholdRangeRule(var min: Double, var max: Double) : CutoffAuditRule {
    override fun check(item: CutoffAudit): MutableList<String> {
        val value = item.threshold
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("threshold $value is below $min")
        }
        if (value > max) {
            problems.add("threshold $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a cutoff audit. */
data class CutoffAuditValidator(var rules: MutableList<CutoffAuditRule>) {
    fun validate(item: CutoffAudit): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CutoffAudit) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CutoffAudit>): MutableMap<String, MutableList<String>> {
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

fun defaultCutoffAuditValidator(): CutoffAuditValidator {
    return CutoffAuditValidator(
        mutableListOf<CutoffAuditRule>(
            CutoffAuditCodeLengthRule(120),
            CutoffAuditRequiredDepotRule("cutoff audit needs an entry"),
            CutoffAuditDepotLengthRule(86400),
            CutoffAuditThresholdRangeRule(1.2, 2488.7),
        ),
    )
}
