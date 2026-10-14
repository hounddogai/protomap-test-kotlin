package com.acme.transport.terminal.dwell.dwelltemplate

/** A rule a rolling dwell template must pass. Check returns its problems, or none. */
interface RollingDwellTemplateRule {
    fun check(item: RollingDwellTemplate): MutableList<String>
}

data class RollingDwellTemplateCodePrefixRule(var prefix: String) : RollingDwellTemplateRule {
    override fun check(item: RollingDwellTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingDwellTemplateDepotLengthRule(var maxLength: Int) : RollingDwellTemplateRule {
    override fun check(item: RollingDwellTemplate): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDwellTemplateEndsAtRangeRule(var min: Int, var max: Int) : RollingDwellTemplateRule {
    override fun check(item: RollingDwellTemplate): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingDwellTemplatePinnedGuardRule(var limit: Double) : RollingDwellTemplateRule {
    override fun check(item: RollingDwellTemplate): MutableList<String> {
        if (item.pinned && item.fuelLiters > limit) {
            return mutableListOf<String>("fuel liters ${item.fuelLiters} exceeds $limit while pinned")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling dwell template. */
data class RollingDwellTemplateValidator(var rules: MutableList<RollingDwellTemplateRule>) {
    fun validate(item: RollingDwellTemplate): MutableList<String> {
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

    fun requireValid(item: RollingDwellTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDwellTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDwellTemplateValidator(): RollingDwellTemplateValidator {
    return RollingDwellTemplateValidator(
        mutableListOf<RollingDwellTemplateRule>(
            RollingDwellTemplateCodePrefixRule("X"),
            RollingDwellTemplateDepotLengthRule(255),
            RollingDwellTemplateEndsAtRangeRule(64, 86400),
            RollingDwellTemplatePinnedGuardRule(6.8),
        ),
    )
}
