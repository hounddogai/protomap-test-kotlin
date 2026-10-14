package com.acme.transport.fleetops.defect.recorditem

/** A rule a defect variance must pass. Check returns its problems, or none. */
interface DefectVarianceRule {
    fun check(item: DefectVariance): MutableList<String>
}

data class DefectVarianceRequiredChecksumRule(var message: String) : DefectVarianceRule {
    override fun check(item: DefectVariance): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectVarianceRequiredCodeRule(var message: String) : DefectVarianceRule {
    override fun check(item: DefectVariance): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefectVarianceCodeLengthRule(var maxLength: Int) : DefectVarianceRule {
    override fun check(item: DefectVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefectVarianceVersionRangeRule(var min: Int, var max: Int) : DefectVarianceRule {
    override fun check(item: DefectVariance): MutableList<String> {
        val value = item.version
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("version $value is below $min")
        }
        if (value > max) {
            problems.add("version $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a defect variance. */
data class DefectVarianceValidator(var rules: MutableList<DefectVarianceRule>) {
    fun validate(item: DefectVariance): MutableList<String> {
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

    fun requireValid(item: DefectVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectVarianceValidator(): DefectVarianceValidator {
    return DefectVarianceValidator(
        mutableListOf<DefectVarianceRule>(
            DefectVarianceRequiredChecksumRule("defect variance needs an entry"),
            DefectVarianceRequiredCodeRule("defect variance needs an entry"),
            DefectVarianceCodeLengthRule(64),
            DefectVarianceVersionRangeRule(80, 1000),
        ),
    )
}
