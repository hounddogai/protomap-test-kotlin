package com.acme.transport.fleetops.defect.bundle

/** A rule a scheduled defect claim must pass. Check returns its problems, or none. */
interface ScheduledDefectClaimRule {
    fun check(item: ScheduledDefectClaim): MutableList<String>
}

data class ScheduledDefectClaimTitleLengthRule(var maxLength: Int) : ScheduledDefectClaimRule {
    override fun check(item: ScheduledDefectClaim): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectClaimCodeLengthRule(var maxLength: Int) : ScheduledDefectClaimRule {
    override fun check(item: ScheduledDefectClaim): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectClaimCodePrefixRule(var prefix: String) : ScheduledDefectClaimRule {
    override fun check(item: ScheduledDefectClaim): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledDefectClaimRouteCodeLengthRule(var maxLength: Int) : ScheduledDefectClaimRule {
    override fun check(item: ScheduledDefectClaim): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled defect claim. */
data class ScheduledDefectClaimValidator(var rules: MutableList<ScheduledDefectClaimRule>) {
    fun validate(item: ScheduledDefectClaim): MutableList<String> {
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

    fun requireValid(item: ScheduledDefectClaim) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledDefectClaim>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledDefectClaimValidator(): ScheduledDefectClaimValidator {
    return ScheduledDefectClaimValidator(
        mutableListOf<ScheduledDefectClaimRule>(
            ScheduledDefectClaimTitleLengthRule(32),
            ScheduledDefectClaimCodeLengthRule(120),
            ScheduledDefectClaimCodePrefixRule("OPS"),
            ScheduledDefectClaimRouteCodeLengthRule(80),
        ),
    )
}
