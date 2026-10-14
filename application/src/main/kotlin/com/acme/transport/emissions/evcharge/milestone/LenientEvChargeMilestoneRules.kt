package com.acme.transport.emissions.evcharge.milestone

/** A rule a lenient ev charge milestone must pass. Check returns its problems, or none. */
interface LenientEvChargeMilestoneRule {
    fun check(item: LenientEvChargeMilestone): MutableList<String>
}

data class LenientEvChargeMilestoneActiveGuardRule(var limit: Int) : LenientEvChargeMilestoneRule {
    override fun check(item: LenientEvChargeMilestone): MutableList<String> {
        if (item.active && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class LenientEvChargeMilestoneOwnerIdLengthRule(var maxLength: Int) : LenientEvChargeMilestoneRule {
    override fun check(item: LenientEvChargeMilestone): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientEvChargeMilestoneRequiredCodeRule(var message: String) : LenientEvChargeMilestoneRule {
    override fun check(item: LenientEvChargeMilestone): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientEvChargeMilestoneRequiredNameRule(var message: String) : LenientEvChargeMilestoneRule {
    override fun check(item: LenientEvChargeMilestone): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient ev charge milestone. */
data class LenientEvChargeMilestoneValidator(var rules: MutableList<LenientEvChargeMilestoneRule>) {
    fun validate(item: LenientEvChargeMilestone): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientEvChargeMilestone) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientEvChargeMilestone>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientEvChargeMilestoneValidator(): LenientEvChargeMilestoneValidator {
    return LenientEvChargeMilestoneValidator(
        mutableListOf<LenientEvChargeMilestoneRule>(
            LenientEvChargeMilestoneActiveGuardRule(32),
            LenientEvChargeMilestoneOwnerIdLengthRule(3600),
            LenientEvChargeMilestoneRequiredCodeRule("lenient ev charge milestone needs a value"),
            LenientEvChargeMilestoneRequiredNameRule("lenient ev charge milestone needs a value"),
        ),
    )
}
