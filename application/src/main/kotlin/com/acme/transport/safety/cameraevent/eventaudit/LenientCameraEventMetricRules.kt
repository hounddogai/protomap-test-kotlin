package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.text.normalize

/** A rule a lenient camera event metric must pass. Check returns its problems, or none. */
interface LenientCameraEventMetricRule {
    fun check(item: LenientCameraEventMetric): MutableList<String>
}

data class LenientCameraEventMetricAllowedShiftCodeRule(
    var allowed: MutableList<String>,
) : LenientCameraEventMetricRule {
    override fun check(item: LenientCameraEventMetric): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class LenientCameraEventMetricActiveGuardRule(var limit: Int) : LenientCameraEventMetricRule {
    override fun check(item: LenientCameraEventMetric): MutableList<String> {
        if (item.active && item.endsAt > limit) {
            return mutableListOf<String>("ends at ${item.endsAt} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class LenientCameraEventMetricDiscountRangeRule(
    var min: Double,
    var max: Double,
) : LenientCameraEventMetricRule {
    override fun check(item: LenientCameraEventMetric): MutableList<String> {
        val value = item.discount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("discount $value is below $min")
        }
        if (value > max) {
            problems.add("discount $value is above $max")
        }
        return problems
    }
}

data class LenientCameraEventMetricRequiredCountryRule(var message: String) : LenientCameraEventMetricRule {
    override fun check(item: LenientCameraEventMetric): MutableList<String> {
        if (item.country.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCameraEventMetricRequiredTitleRule(var message: String) : LenientCameraEventMetricRule {
    override fun check(item: LenientCameraEventMetric): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient camera event metric. */
data class LenientCameraEventMetricValidator(var rules: MutableList<LenientCameraEventMetricRule>) {
    fun validate(item: LenientCameraEventMetric): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LenientCameraEventMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCameraEventMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCameraEventMetricValidator(): LenientCameraEventMetricValidator {
    return LenientCameraEventMetricValidator(
        mutableListOf<LenientCameraEventMetricRule>(
            LenientCameraEventMetricAllowedShiftCodeRule(mutableListOf<String>()),
            LenientCameraEventMetricActiveGuardRule(16),
            LenientCameraEventMetricDiscountRangeRule(3.1, 2720.4),
            LenientCameraEventMetricRequiredCountryRule("lenient camera event metric needs this field"),
            LenientCameraEventMetricRequiredTitleRule("lenient camera event metric needs a value"),
        ),
    )
}
