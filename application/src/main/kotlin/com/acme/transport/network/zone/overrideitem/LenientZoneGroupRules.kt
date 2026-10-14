package com.acme.transport.network.zone.overrideitem

/** A rule a lenient zone group must pass. Check returns its problems, or none. */
interface LenientZoneGroupRule {
    fun check(item: LenientZoneGroup): MutableList<String>
}

data class LenientZoneGroupCreatedAtRangeRule(var min: Int, var max: Int) : LenientZoneGroupRule {
    override fun check(item: LenientZoneGroup): MutableList<String> {
        val value = item.createdAt
        if (value < min || value > max) {
            return mutableListOf<String>("created at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientZoneGroupWeightRangeRule(var min: Double, var max: Double) : LenientZoneGroupRule {
    override fun check(item: LenientZoneGroup): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LenientZoneGroupRequiredCodeRule(var message: String) : LenientZoneGroupRule {
    override fun check(item: LenientZoneGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientZoneGroupWindowDaysRangeRule(var min: Int, var max: Int) : LenientZoneGroupRule {
    override fun check(item: LenientZoneGroup): MutableList<String> {
        val value = item.windowDays
        if (value < min || value > max) {
            return mutableListOf<String>("window days $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient zone group. */
data class LenientZoneGroupValidator(var rules: MutableList<LenientZoneGroupRule>) {
    fun validate(item: LenientZoneGroup): MutableList<String> {
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

    fun requireValid(item: LenientZoneGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientZoneGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientZoneGroupValidator(): LenientZoneGroupValidator {
    return LenientZoneGroupValidator(
        mutableListOf<LenientZoneGroupRule>(
            LenientZoneGroupCreatedAtRangeRule(80, 120),
            LenientZoneGroupWeightRangeRule(5.8, 3491.1),
            LenientZoneGroupRequiredCodeRule("lenient zone group needs an entry"),
            LenientZoneGroupWindowDaysRangeRule(80, 86400),
        ),
    )
}
