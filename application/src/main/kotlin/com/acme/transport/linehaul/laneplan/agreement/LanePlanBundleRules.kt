package com.acme.transport.linehaul.laneplan.agreement

/** A rule a lane plan bundle must pass. Check returns its problems, or none. */
interface LanePlanBundleRule {
    fun check(item: LanePlanBundle): MutableList<String>
}

data class LanePlanBundleVisibleGuardRule(var limit: Int) : LanePlanBundleRule {
    override fun check(item: LanePlanBundle): MutableList<String> {
        if (item.visible && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanBundleLimitRangeRule(var min: Int, var max: Int) : LanePlanBundleRule {
    override fun check(item: LanePlanBundle): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanBundleRevisionRangeRule(var min: Int, var max: Int) : LanePlanBundleRule {
    override fun check(item: LanePlanBundle): MutableList<String> {
        val value = item.revision
        if (value < min || value > max) {
            return mutableListOf<String>("revision $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanBundleRequiredNameRule(var message: String) : LanePlanBundleRule {
    override fun check(item: LanePlanBundle): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanBundleAttemptsRangeRule(var min: Int, var max: Int) : LanePlanBundleRule {
    override fun check(item: LanePlanBundle): MutableList<String> {
        val value = item.attempts
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("attempts $value is below $min")
        }
        if (value > max) {
            problems.add("attempts $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a lane plan bundle. */
data class LanePlanBundleValidator(var rules: MutableList<LanePlanBundleRule>) {
    fun validate(item: LanePlanBundle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LanePlanBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanBundleValidator(): LanePlanBundleValidator {
    return LanePlanBundleValidator(
        mutableListOf<LanePlanBundleRule>(
            LanePlanBundleVisibleGuardRule(255),
            LanePlanBundleLimitRangeRule(80, 255),
            LanePlanBundleRevisionRangeRule(64, 1000),
            LanePlanBundleRequiredNameRule("lane plan bundle needs a value"),
            LanePlanBundleAttemptsRangeRule(255, 3600),
        ),
    )
}
