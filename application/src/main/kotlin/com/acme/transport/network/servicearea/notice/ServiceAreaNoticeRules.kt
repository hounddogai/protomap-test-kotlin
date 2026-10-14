package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.text.normalize

/** A rule a service area notice must pass. Check returns its problems, or none. */
interface ServiceAreaNoticeRule {
    fun check(item: ServiceAreaNotice): MutableList<String>
}

data class ServiceAreaNoticeAllowedCodeRule(var allowed: MutableList<String>) : ServiceAreaNoticeRule {
    override fun check(item: ServiceAreaNotice): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaNoticeMinimumRangeRule(var min: Double, var max: Double) : ServiceAreaNoticeRule {
    override fun check(item: ServiceAreaNotice): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

data class ServiceAreaNoticeEtaMinutesRangeRule(var min: Int, var max: Int) : ServiceAreaNoticeRule {
    override fun check(item: ServiceAreaNotice): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaNoticeCodeLengthRule(var maxLength: Int) : ServiceAreaNoticeRule {
    override fun check(item: ServiceAreaNotice): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area notice. */
data class ServiceAreaNoticeValidator(var rules: MutableList<ServiceAreaNoticeRule>) {
    fun validate(item: ServiceAreaNotice): MutableList<String> {
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

    fun requireValid(item: ServiceAreaNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaNoticeValidator(): ServiceAreaNoticeValidator {
    return ServiceAreaNoticeValidator(
        mutableListOf<ServiceAreaNoticeRule>(
            ServiceAreaNoticeAllowedCodeRule(mutableListOf<String>("lenient", "weighted", "seasonal")),
            ServiceAreaNoticeMinimumRangeRule(6.2, 2463.9),
            ServiceAreaNoticeEtaMinutesRangeRule(64, 3600),
            ServiceAreaNoticeCodeLengthRule(32),
        ),
    )
}
