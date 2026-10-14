package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.kit.text.normalize

/** A rule a adaptive service plan notice must pass. Check returns its problems, or none. */
interface AdaptiveServicePlanNoticeRule {
    fun check(item: AdaptiveServicePlanNotice): MutableList<String>
}

data class AdaptiveServicePlanNoticeLabelLengthRule(var maxLength: Int) : AdaptiveServicePlanNoticeRule {
    override fun check(item: AdaptiveServicePlanNotice): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServicePlanNoticeRegionLengthRule(var maxLength: Int) : AdaptiveServicePlanNoticeRule {
    override fun check(item: AdaptiveServicePlanNotice): MutableList<String> {
        val size = item.region.length
        if (size > maxLength) {
            return mutableListOf<String>("region is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServicePlanNoticeAllowedLabelRule(
    var allowed: MutableList<String>,
) : AdaptiveServicePlanNoticeRule {
    override fun check(item: AdaptiveServicePlanNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServicePlanNoticeLocaleLengthRule(var maxLength: Int) : AdaptiveServicePlanNoticeRule {
    override fun check(item: AdaptiveServicePlanNotice): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveServicePlanNoticeCodeLengthRule(var maxLength: Int) : AdaptiveServicePlanNoticeRule {
    override fun check(item: AdaptiveServicePlanNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive service plan notice. */
data class AdaptiveServicePlanNoticeValidator(var rules: MutableList<AdaptiveServicePlanNoticeRule>) {
    fun validate(item: AdaptiveServicePlanNotice): MutableList<String> {
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

    fun requireValid(item: AdaptiveServicePlanNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveServicePlanNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveServicePlanNoticeValidator(): AdaptiveServicePlanNoticeValidator {
    return AdaptiveServicePlanNoticeValidator(
        mutableListOf<AdaptiveServicePlanNoticeRule>(
            AdaptiveServicePlanNoticeLabelLengthRule(16),
            AdaptiveServicePlanNoticeRegionLengthRule(64),
            AdaptiveServicePlanNoticeAllowedLabelRule(mutableListOf<String>("adaptive", "adaptive")),
            AdaptiveServicePlanNoticeLocaleLengthRule(255),
            AdaptiveServicePlanNoticeCodeLengthRule(64),
        ),
    )
}
