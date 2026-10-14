package com.acme.transport.fleetops.serviceplan.agreement

/** A rule a manual service plan segment must pass. Check returns its problems, or none. */
interface ManualServicePlanSegmentRule {
    fun check(item: ManualServicePlanSegment): MutableList<String>
}

data class ManualServicePlanSegmentCodeLengthRule(var maxLength: Int) : ManualServicePlanSegmentRule {
    override fun check(item: ManualServicePlanSegment): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanSegmentTitleLengthRule(var maxLength: Int) : ManualServicePlanSegmentRule {
    override fun check(item: ManualServicePlanSegment): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanSegmentRequiredLocaleRule(var message: String) : ManualServicePlanSegmentRule {
    override fun check(item: ManualServicePlanSegment): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualServicePlanSegmentRequiredCodeRule(var message: String) : ManualServicePlanSegmentRule {
    override fun check(item: ManualServicePlanSegment): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual service plan segment. */
data class ManualServicePlanSegmentValidator(var rules: MutableList<ManualServicePlanSegmentRule>) {
    fun validate(item: ManualServicePlanSegment): MutableList<String> {
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

    fun requireValid(item: ManualServicePlanSegment) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualServicePlanSegment>): MutableMap<String, MutableList<String>> {
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

fun defaultManualServicePlanSegmentValidator(): ManualServicePlanSegmentValidator {
    return ManualServicePlanSegmentValidator(
        mutableListOf<ManualServicePlanSegmentRule>(
            ManualServicePlanSegmentCodeLengthRule(64),
            ManualServicePlanSegmentTitleLengthRule(16),
            ManualServicePlanSegmentRequiredLocaleRule("manual service plan segment needs an entry"),
            ManualServicePlanSegmentRequiredCodeRule("manual service plan segment needs a value"),
        ),
    )
}
