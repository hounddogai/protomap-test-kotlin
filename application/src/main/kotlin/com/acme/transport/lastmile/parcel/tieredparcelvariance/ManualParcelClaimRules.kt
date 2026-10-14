package com.acme.transport.lastmile.parcel.tieredparcelvariance

/** A rule a manual parcel claim must pass. Check returns its problems, or none. */
interface ManualParcelClaimRule {
    fun check(item: ManualParcelClaim): MutableList<String>
}

data class ManualParcelClaimNameLengthRule(var maxLength: Int) : ManualParcelClaimRule {
    override fun check(item: ManualParcelClaim): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelClaimActiveGuardRule(var limit: Int) : ManualParcelClaimRule {
    override fun check(item: ManualParcelClaim): MutableList<String> {
        if (item.active && item.retries > limit) {
            return mutableListOf<String>("retries ${item.retries} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelClaimSequenceRangeRule(var min: Int, var max: Int) : ManualParcelClaimRule {
    override fun check(item: ManualParcelClaim): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a manual parcel claim. */
data class ManualParcelClaimValidator(var rules: MutableList<ManualParcelClaimRule>) {
    fun validate(item: ManualParcelClaim): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualParcelClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualParcelClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultManualParcelClaimValidator(): ManualParcelClaimValidator {
    return ManualParcelClaimValidator(
        mutableListOf<ManualParcelClaimRule>(
            ManualParcelClaimNameLengthRule(255),
            ManualParcelClaimActiveGuardRule(255),
            ManualParcelClaimSequenceRangeRule(16, 120),
        ),
    )
}
