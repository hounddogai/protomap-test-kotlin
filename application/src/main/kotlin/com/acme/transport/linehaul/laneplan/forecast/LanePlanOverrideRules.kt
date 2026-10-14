package com.acme.transport.linehaul.laneplan.forecast

/** A rule a lane plan override must pass. Check returns its problems, or none. */
interface LanePlanOverrideRule {
    fun check(item: LanePlanOverride): MutableList<String>
}

data class LanePlanOverrideLabelLengthRule(var maxLength: Int) : LanePlanOverrideRule {
    override fun check(item: LanePlanOverride): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanOverrideRequiredCodeRule(var message: String) : LanePlanOverrideRule {
    override fun check(item: LanePlanOverride): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LanePlanOverrideDescriptionLengthRule(var maxLength: Int) : LanePlanOverrideRule {
    override fun check(item: LanePlanOverride): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanOverrideCodePrefixRule(var prefix: String) : LanePlanOverrideRule {
    override fun check(item: LanePlanOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LanePlanOverrideRequiredDescriptionRule(var message: String) : LanePlanOverrideRule {
    override fun check(item: LanePlanOverride): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lane plan override. */
data class LanePlanOverrideValidator(var rules: MutableList<LanePlanOverrideRule>) {
    fun validate(item: LanePlanOverride): MutableList<String> {
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

    fun requireValid(item: LanePlanOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LanePlanOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultLanePlanOverrideValidator(): LanePlanOverrideValidator {
    return LanePlanOverrideValidator(
        mutableListOf<LanePlanOverrideRule>(
            LanePlanOverrideLabelLengthRule(86400),
            LanePlanOverrideRequiredCodeRule("lane plan override needs an entry"),
            LanePlanOverrideDescriptionLengthRule(32),
            LanePlanOverrideCodePrefixRule("REF"),
            LanePlanOverrideRequiredDescriptionRule("lane plan override needs an entry"),
        ),
    )
}
