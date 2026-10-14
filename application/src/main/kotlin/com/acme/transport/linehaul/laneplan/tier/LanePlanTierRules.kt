package com.acme.transport.linehaul.laneplan.tier

/** A rule a lane plan tier must pass. Check returns its problems, or none. */
interface LanePlanTierRule {
    fun check(item: LanePlanTier): MutableList<String>
}

data class LanePlanTierRequiredCodeRule(var message: String) : LanePlanTierRule {
    override fun check(item: LanePlanTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanTierCodePrefixRule(var prefix: String) : LanePlanTierRule {
    override fun check(item: LanePlanTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanTierActiveGuardRule(var limit: Int) : LanePlanTierRule {
    override fun check(item: LanePlanTier): MutableList<String> {
        if (item.active && item.updatedAt > limit) {
            return mutableListOf<String>("updated at ${item.updatedAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanTierRequiredShiftCodeRule(var message: String) : LanePlanTierRule {
    override fun check(item: LanePlanTier): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan tier. */
data class LanePlanTierValidator(var rules: MutableList<LanePlanTierRule>) {
    fun validate(item: LanePlanTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LanePlanTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanTier>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanTierValidator(): LanePlanTierValidator {
    return LanePlanTierValidator(
        mutableListOf<LanePlanTierRule>(
            LanePlanTierRequiredCodeRule("lane plan tier needs an entry"),
            LanePlanTierCodePrefixRule("REF"),
            LanePlanTierActiveGuardRule(64),
            LanePlanTierRequiredShiftCodeRule("lane plan tier needs a value"),
        ),
    )
}
