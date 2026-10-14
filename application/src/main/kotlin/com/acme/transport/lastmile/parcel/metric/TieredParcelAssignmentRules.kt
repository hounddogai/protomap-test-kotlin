package com.acme.transport.lastmile.parcel.metric

/** A rule a tiered parcel assignment must pass. Check returns its problems, or none. */
interface TieredParcelAssignmentRule {
    fun check(item: TieredParcelAssignment): MutableList<String>
}

data class TieredParcelAssignmentRequiredNameRule(var message: String) : TieredParcelAssignmentRule {
    override fun check(item: TieredParcelAssignment): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelAssignmentRetriesRangeRule(var min: Int, var max: Int) : TieredParcelAssignmentRule {
    override fun check(item: TieredParcelAssignment): MutableList<String> {
        val value = item.retries
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("retries $value is below $min")
        }
        if (value > max) {
            problems.add("retries $value is above $max")
        }
        return problems
    }
}

data class TieredParcelAssignmentCodePrefixRule(var prefix: String) : TieredParcelAssignmentRule {
    override fun check(item: TieredParcelAssignment): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelAssignmentRequiredCodeRule(var message: String) : TieredParcelAssignmentRule {
    override fun check(item: TieredParcelAssignment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredParcelAssignmentNameLengthRule(var maxLength: Int) : TieredParcelAssignmentRule {
    override fun check(item: TieredParcelAssignment): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered parcel assignment. */
data class TieredParcelAssignmentValidator(var rules: MutableList<TieredParcelAssignmentRule>) {
    fun validate(item: TieredParcelAssignment): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredParcelAssignment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredParcelAssignment>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredParcelAssignmentValidator(): TieredParcelAssignmentValidator {
    return TieredParcelAssignmentValidator(
        mutableListOf<TieredParcelAssignmentRule>(
            TieredParcelAssignmentRequiredNameRule("tiered parcel assignment needs this field"),
            TieredParcelAssignmentRetriesRangeRule(16, 1000),
            TieredParcelAssignmentCodePrefixRule("Q"),
            TieredParcelAssignmentRequiredCodeRule("tiered parcel assignment needs a value"),
            TieredParcelAssignmentNameLengthRule(255),
        ),
    )
}
