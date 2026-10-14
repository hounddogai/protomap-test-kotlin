package com.acme.transport.safety.violation.forecast

/** A rule a capped violation mapping must pass. Check returns its problems, or none. */
interface CappedViolationMappingRule {
    fun check(item: CappedViolationMapping): MutableList<String>
}

data class CappedViolationMappingSequenceRangeRule(var min: Int, var max: Int) : CappedViolationMappingRule {
    override fun check(item: CappedViolationMapping): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationMappingCodeLengthRule(var maxLength: Int) : CappedViolationMappingRule {
    override fun check(item: CappedViolationMapping): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationMappingRateRangeRule(var min: Double, var max: Double) : CappedViolationMappingRule {
    override fun check(item: CappedViolationMapping): MutableList<String> {
        val value = item.rate
        if (value < min || value > max) {
            return mutableListOf<String>("rate $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedViolationMappingApprovedGuardRule(var limit: Double) : CappedViolationMappingRule {
    override fun check(item: CappedViolationMapping): MutableList<String> {
        if (item.approved && item.floorValue > limit) {
            return mutableListOf<String>("floor value ${item.floorValue} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped violation mapping. */
data class CappedViolationMappingValidator(var rules: MutableList<CappedViolationMappingRule>) {
    fun validate(item: CappedViolationMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CappedViolationMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedViolationMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedViolationMappingValidator(): CappedViolationMappingValidator {
    return CappedViolationMappingValidator(
        mutableListOf<CappedViolationMappingRule>(
            CappedViolationMappingSequenceRangeRule(64, 3600),
            CappedViolationMappingCodeLengthRule(16),
            CappedViolationMappingRateRangeRule(8.2, 3324.0),
            CappedViolationMappingApprovedGuardRule(9.1),
        ),
    )
}
