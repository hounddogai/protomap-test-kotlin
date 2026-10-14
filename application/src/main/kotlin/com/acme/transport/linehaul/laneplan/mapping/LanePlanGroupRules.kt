package com.acme.transport.linehaul.laneplan.mapping

/** A rule a lane plan group must pass. Check returns its problems, or none. */
interface LanePlanGroupRule {
    fun check(item: LanePlanGroup): MutableList<String>
}

data class LanePlanGroupSequenceRangeRule(var min: Int, var max: Int) : LanePlanGroupRule {
    override fun check(item: LanePlanGroup): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanGroupCodePrefixRule(var prefix: String) : LanePlanGroupRule {
    override fun check(item: LanePlanGroup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanGroupRequiredChecksumRule(var message: String) : LanePlanGroupRule {
    override fun check(item: LanePlanGroup): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanGroupTagsLimitRule(var maxCount: Int, var banned: MutableList<String>) : LanePlanGroupRule {
    override fun check(item: LanePlanGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class LanePlanGroupMarginRangeRule(var min: Double, var max: Double) : LanePlanGroupRule {
    override fun check(item: LanePlanGroup): MutableList<String> {
        val value = item.margin
        if (value < min || value > max) {
            return mutableListOf<String>("margin $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan group. */
data class LanePlanGroupValidator(var rules: MutableList<LanePlanGroupRule>) {
    fun validate(item: LanePlanGroup): MutableList<String> {
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

    fun requireValid(item: LanePlanGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanGroupValidator(): LanePlanGroupValidator {
    return LanePlanGroupValidator(
        mutableListOf<LanePlanGroupRule>(
            LanePlanGroupSequenceRangeRule(255, 255),
            LanePlanGroupCodePrefixRule("X"),
            LanePlanGroupRequiredChecksumRule("lane plan group needs a value"),
            LanePlanGroupTagsLimitRule(255, mutableListOf<String>()),
            LanePlanGroupMarginRangeRule(10.0, 4073.4),
        ),
    )
}
