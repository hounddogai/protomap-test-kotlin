package com.acme.transport.crewing.availability.scheduledavailabilitytier

/** A rule a regional availability group must pass. Check returns its problems, or none. */
interface RegionalAvailabilityGroupRule {
    fun check(item: RegionalAvailabilityGroup): MutableList<String>
}

data class RegionalAvailabilityGroupRatioRangeRule(
    var min: Double,
    var max: Double,
) : RegionalAvailabilityGroupRule {
    override fun check(item: RegionalAvailabilityGroup): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAvailabilityGroupTitleLengthRule(var maxLength: Int) : RegionalAvailabilityGroupRule {
    override fun check(item: RegionalAvailabilityGroup): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAvailabilityGroupCodeLengthRule(var maxLength: Int) : RegionalAvailabilityGroupRule {
    override fun check(item: RegionalAvailabilityGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalAvailabilityGroupRequiredCodeRule(var message: String) : RegionalAvailabilityGroupRule {
    override fun check(item: RegionalAvailabilityGroup): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalAvailabilityGroupTimezoneLengthRule(var maxLength: Int) : RegionalAvailabilityGroupRule {
    override fun check(item: RegionalAvailabilityGroup): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional availability group. */
data class RegionalAvailabilityGroupValidator(var rules: MutableList<RegionalAvailabilityGroupRule>) {
    fun validate(item: RegionalAvailabilityGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalAvailabilityGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalAvailabilityGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalAvailabilityGroupValidator(): RegionalAvailabilityGroupValidator {
    return RegionalAvailabilityGroupValidator(
        mutableListOf<RegionalAvailabilityGroupRule>(
            RegionalAvailabilityGroupRatioRangeRule(3.8, 2974.4),
            RegionalAvailabilityGroupTitleLengthRule(16),
            RegionalAvailabilityGroupCodeLengthRule(32),
            RegionalAvailabilityGroupRequiredCodeRule("regional availability group needs a value"),
            RegionalAvailabilityGroupTimezoneLengthRule(3600),
        ),
    )
}
