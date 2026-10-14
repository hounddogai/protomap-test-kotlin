package com.acme.transport.fleetops.serviceplan.planrollup

/** A rule a fallback service plan notice must pass. Check returns its problems, or none. */
interface FallbackServicePlanNoticeRule {
    fun check(item: FallbackServicePlanNotice): MutableList<String>
}

data class FallbackServicePlanNoticeRequiredTitleRule(var message: String) : FallbackServicePlanNoticeRule {
    override fun check(item: FallbackServicePlanNotice): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackServicePlanNoticeTitleLengthRule(var maxLength: Int) : FallbackServicePlanNoticeRule {
    override fun check(item: FallbackServicePlanNotice): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackServicePlanNoticeRequiredDescriptionRule(
    var message: String,
) : FallbackServicePlanNoticeRule {
    override fun check(item: FallbackServicePlanNotice): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackServicePlanNoticeDescriptionLengthRule(var maxLength: Int) : FallbackServicePlanNoticeRule {
    override fun check(item: FallbackServicePlanNotice): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback service plan notice. */
data class FallbackServicePlanNoticeValidator(var rules: MutableList<FallbackServicePlanNoticeRule>) {
    fun validate(item: FallbackServicePlanNotice): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackServicePlanNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackServicePlanNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackServicePlanNoticeValidator(): FallbackServicePlanNoticeValidator {
    return FallbackServicePlanNoticeValidator(
        mutableListOf<FallbackServicePlanNoticeRule>(
            FallbackServicePlanNoticeRequiredTitleRule("fallback service plan notice needs an entry"),
            FallbackServicePlanNoticeTitleLengthRule(255),
            FallbackServicePlanNoticeRequiredDescriptionRule("fallback service plan notice needs this field"),
            FallbackServicePlanNoticeDescriptionLengthRule(1000),
        ),
    )
}
