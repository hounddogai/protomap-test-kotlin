package com.acme.transport.safety.nearmiss.recorditem

/** A rule a seasonal near miss assignment must pass. Check returns its problems, or none. */
interface SeasonalNearMissAssignmentRule {
    fun check(item: SeasonalNearMissAssignment): MutableList<String>
}

data class SeasonalNearMissAssignmentBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : SeasonalNearMissAssignmentRule {
    override fun check(item: SeasonalNearMissAssignment): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNearMissAssignmentRequiredOwnerIdRule(var message: String) : SeasonalNearMissAssignmentRule {
    override fun check(item: SeasonalNearMissAssignment): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNearMissAssignmentCodePrefixRule(var prefix: String) : SeasonalNearMissAssignmentRule {
    override fun check(item: SeasonalNearMissAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNearMissAssignmentNameLengthRule(var maxLength: Int) : SeasonalNearMissAssignmentRule {
    override fun check(item: SeasonalNearMissAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalNearMissAssignmentRequiredCodeRule(var message: String) : SeasonalNearMissAssignmentRule {
    override fun check(item: SeasonalNearMissAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal near miss assignment. */
data class SeasonalNearMissAssignmentValidator(var rules: MutableList<SeasonalNearMissAssignmentRule>) {
    fun validate(item: SeasonalNearMissAssignment): MutableList<String> {
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

    fun requireValid(item: SeasonalNearMissAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalNearMissAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalNearMissAssignmentValidator(): SeasonalNearMissAssignmentValidator {
    return SeasonalNearMissAssignmentValidator(
        mutableListOf<SeasonalNearMissAssignmentRule>(
            SeasonalNearMissAssignmentBatchSizeRangeRule(16, 120),
            SeasonalNearMissAssignmentRequiredOwnerIdRule("seasonal near miss assignment needs a value"),
            SeasonalNearMissAssignmentCodePrefixRule("REF"),
            SeasonalNearMissAssignmentNameLengthRule(120),
            SeasonalNearMissAssignmentRequiredCodeRule("seasonal near miss assignment needs this field"),
        ),
    )
}
