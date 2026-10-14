package com.acme.transport.customs.inspection.inspectiontarget

/** A rule a regional inspection target must pass. Check returns its problems, or none. */
interface RegionalInspectionTargetRule {
    fun check(item: RegionalInspectionTarget): MutableList<String>
}

data class RegionalInspectionTargetNameLengthRule(var maxLength: Int) : RegionalInspectionTargetRule {
    override fun check(item: RegionalInspectionTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionTargetEndsAtRangeRule(var min: Int, var max: Int) : RegionalInspectionTargetRule {
    override fun check(item: RegionalInspectionTarget): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionTargetUnitLengthRule(var maxLength: Int) : RegionalInspectionTargetRule {
    override fun check(item: RegionalInspectionTarget): MutableList<String> {
        val size = item.unit.length
        if (size > maxLength) {
            return mutableListOf<String>("unit is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionTargetRequiredCodeRule(var message: String) : RegionalInspectionTargetRule {
    override fun check(item: RegionalInspectionTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalInspectionTargetRequiredNameRule(var message: String) : RegionalInspectionTargetRule {
    override fun check(item: RegionalInspectionTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional inspection target. */
data class RegionalInspectionTargetValidator(var rules: MutableList<RegionalInspectionTargetRule>) {
    fun validate(item: RegionalInspectionTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalInspectionTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalInspectionTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalInspectionTargetValidator(): RegionalInspectionTargetValidator {
    return RegionalInspectionTargetValidator(
        mutableListOf<RegionalInspectionTargetRule>(
            RegionalInspectionTargetNameLengthRule(1000),
            RegionalInspectionTargetEndsAtRangeRule(32, 3600),
            RegionalInspectionTargetUnitLengthRule(255),
            RegionalInspectionTargetRequiredCodeRule("regional inspection target needs an entry"),
            RegionalInspectionTargetRequiredNameRule("regional inspection target needs an entry"),
        ),
    )
}
