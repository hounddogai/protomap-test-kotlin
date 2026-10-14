package com.acme.transport.fleetops.defect.proposal

/** A rule a defect digest must pass. Check returns its problems, or none. */
interface DefectDigestRule {
    fun check(item: DefectDigest): MutableList<String>
}

data class DefectDigestStartsAtRangeRule(var min: Int, var max: Int) : DefectDigestRule {
    override fun check(item: DefectDigest): MutableList<String> {
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

data class DefectDigestOdometerRangeRule(var min: Int, var max: Int) : DefectDigestRule {
    override fun check(item: DefectDigest): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

data class DefectDigestRequiredLabelRule(var message: String) : DefectDigestRule {
    override fun check(item: DefectDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect digest. */
data class DefectDigestValidator(var rules: MutableList<DefectDigestRule>) {
    fun validate(item: DefectDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectDigestValidator(): DefectDigestValidator {
    return DefectDigestValidator(
        mutableListOf<DefectDigestRule>(
            DefectDigestStartsAtRangeRule(64, 80),
            DefectDigestOdometerRangeRule(120, 120),
            DefectDigestRequiredLabelRule("defect digest needs this field"),
        ),
    )
}
