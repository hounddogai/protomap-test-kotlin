package com.acme.transport.linehaul.laneplan.planrule

/** A rule a lane plan score must pass. Check returns its problems, or none. */
interface LanePlanScoreRule {
    fun check(item: LanePlanScore): MutableList<String>
}

data class LanePlanScoreNameLengthRule(var maxLength: Int) : LanePlanScoreRule {
    override fun check(item: LanePlanScore): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanScoreRequiredNameRule(var message: String) : LanePlanScoreRule {
    override fun check(item: LanePlanScore): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanScoreCodePrefixRule(var prefix: String) : LanePlanScoreRule {
    override fun check(item: LanePlanScore): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanScoreRequiredCodeRule(var message: String) : LanePlanScoreRule {
    override fun check(item: LanePlanScore): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan score. */
data class LanePlanScoreValidator(var rules: MutableList<LanePlanScoreRule>) {
    fun validate(item: LanePlanScore): MutableList<String> {
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

    fun requireValid(item: LanePlanScore) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanScore>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanScoreValidator(): LanePlanScoreValidator {
    return LanePlanScoreValidator(
        mutableListOf<LanePlanScoreRule>(
            LanePlanScoreNameLengthRule(64),
            LanePlanScoreRequiredNameRule("lane plan score needs a value"),
            LanePlanScoreCodePrefixRule("OPS"),
            LanePlanScoreRequiredCodeRule("lane plan score needs a value"),
        ),
    )
}
