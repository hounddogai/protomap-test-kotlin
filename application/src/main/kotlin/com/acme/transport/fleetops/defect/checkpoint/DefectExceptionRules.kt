package com.acme.transport.fleetops.defect.checkpoint

/** A rule a defect exception must pass. Check returns its problems, or none. */
interface DefectExceptionRule {
    fun check(item: DefectException): MutableList<String>
}

data class DefectExceptionApprovedGuardRule(var limit: Int) : DefectExceptionRule {
    override fun check(item: DefectException): MutableList<String> {
        if (item.approved && item.startsAt > limit) {
            return mutableListOf<String>("starts at ${item.startsAt} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class DefectExceptionLimitRangeRule(var min: Int, var max: Int) : DefectExceptionRule {
    override fun check(item: DefectException): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectExceptionCodePrefixRule(var prefix: String) : DefectExceptionRule {
    override fun check(item: DefectException): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefectExceptionNameLengthRule(var maxLength: Int) : DefectExceptionRule {
    override fun check(item: DefectException): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect exception. */
data class DefectExceptionValidator(var rules: MutableList<DefectExceptionRule>) {
    fun validate(item: DefectException): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectException) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectException>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectExceptionValidator(): DefectExceptionValidator {
    return DefectExceptionValidator(
        mutableListOf<DefectExceptionRule>(
            DefectExceptionApprovedGuardRule(64),
            DefectExceptionLimitRangeRule(16, 64),
            DefectExceptionCodePrefixRule("OPS"),
            DefectExceptionNameLengthRule(32),
        ),
    )
}
