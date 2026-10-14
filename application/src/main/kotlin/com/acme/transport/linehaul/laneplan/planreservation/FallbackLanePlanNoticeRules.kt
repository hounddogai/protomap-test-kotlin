package com.acme.transport.linehaul.laneplan.planreservation

/** A rule a fallback lane plan notice must pass. Check returns its problems, or none. */
interface FallbackLanePlanNoticeRule {
    fun check(item: FallbackLanePlanNotice): MutableList<String>
}

data class FallbackLanePlanNoticeTimezoneLengthRule(var maxLength: Int) : FallbackLanePlanNoticeRule {
    override fun check(item: FallbackLanePlanNotice): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanNoticeRequiredCodeRule(var message: String) : FallbackLanePlanNoticeRule {
    override fun check(item: FallbackLanePlanNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackLanePlanNoticeRequiredTrailerIdRule(var message: String) : FallbackLanePlanNoticeRule {
    override fun check(item: FallbackLanePlanNotice): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback lane plan notice. */
data class FallbackLanePlanNoticeValidator(var rules: MutableList<FallbackLanePlanNoticeRule>) {
    fun validate(item: FallbackLanePlanNotice): MutableList<String> {
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

    fun requireValid(item: FallbackLanePlanNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackLanePlanNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackLanePlanNoticeValidator(): FallbackLanePlanNoticeValidator {
    return FallbackLanePlanNoticeValidator(
        mutableListOf<FallbackLanePlanNoticeRule>(
            FallbackLanePlanNoticeTimezoneLengthRule(1000),
            FallbackLanePlanNoticeRequiredCodeRule("fallback lane plan notice needs a value"),
            FallbackLanePlanNoticeRequiredTrailerIdRule("fallback lane plan notice needs a value"),
        ),
    )
}
