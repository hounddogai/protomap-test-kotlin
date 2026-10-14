package com.acme.transport.fleetops.defect.agreement

/** A rule a defect estimate must pass. Check returns its problems, or none. */
interface DefectEstimateRule {
    fun check(item: DefectEstimate): MutableList<String>
}

data class DefectEstimateArchivedGuardRule(var limit: Double) : DefectEstimateRule {
    override fun check(item: DefectEstimate): MutableList<String> {
        if (item.archived && item.discount > limit) {
            return mutableListOf<String>("discount ${item.discount} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class DefectEstimateGraceDaysRangeRule(var min: Int, var max: Int) : DefectEstimateRule {
    override fun check(item: DefectEstimate): MutableList<String> {
        val value = item.graceDays
        if (value < min || value > max) {
            return mutableListOf<String>("grace days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefectEstimateCodePrefixRule(var prefix: String) : DefectEstimateRule {
    override fun check(item: DefectEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefectEstimateOwnerIdLengthRule(var maxLength: Int) : DefectEstimateRule {
    override fun check(item: DefectEstimate): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a defect estimate. */
data class DefectEstimateValidator(var rules: MutableList<DefectEstimateRule>) {
    fun validate(item: DefectEstimate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefectEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefectEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultDefectEstimateValidator(): DefectEstimateValidator {
    return DefectEstimateValidator(
        mutableListOf<DefectEstimateRule>(
            DefectEstimateArchivedGuardRule(2.6),
            DefectEstimateGraceDaysRangeRule(120, 86400),
            DefectEstimateCodePrefixRule("OPS"),
            DefectEstimateOwnerIdLengthRule(120),
        ),
    )
}
