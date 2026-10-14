package com.acme.transport.fleetops.serviceplan.assignment

/** A rule a primary service plan notice must pass. Check returns its problems, or none. */
interface PrimaryServicePlanNoticeRule {
    fun check(item: PrimaryServicePlanNotice): MutableList<String>
}

data class PrimaryServicePlanNoticeCapacityRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryServicePlanNoticeRule {
    override fun check(item: PrimaryServicePlanNotice): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class PrimaryServicePlanNoticeCodeLengthRule(var maxLength: Int) : PrimaryServicePlanNoticeRule {
    override fun check(item: PrimaryServicePlanNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServicePlanNoticeRequiredLabelRule(var message: String) : PrimaryServicePlanNoticeRule {
    override fun check(item: PrimaryServicePlanNotice): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryServicePlanNoticeLabelLengthRule(var maxLength: Int) : PrimaryServicePlanNoticeRule {
    override fun check(item: PrimaryServicePlanNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary service plan notice. */
data class PrimaryServicePlanNoticeValidator(var rules: MutableList<PrimaryServicePlanNoticeRule>) {
    fun validate(item: PrimaryServicePlanNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryServicePlanNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryServicePlanNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryServicePlanNoticeValidator(): PrimaryServicePlanNoticeValidator {
    return PrimaryServicePlanNoticeValidator(
        mutableListOf<PrimaryServicePlanNoticeRule>(
            PrimaryServicePlanNoticeCapacityRangeRule(255, 3600),
            PrimaryServicePlanNoticeCodeLengthRule(16),
            PrimaryServicePlanNoticeRequiredLabelRule("primary service plan notice needs an entry"),
            PrimaryServicePlanNoticeLabelLengthRule(64),
        ),
    )
}
