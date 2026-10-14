package com.acme.transport.safety.cameraevent.eventbundle

import com.acme.transport.kit.text.normalize

/** A rule a pending camera event bundle must pass. Check returns its problems, or none. */
interface PendingCameraEventBundleRule {
    fun check(item: PendingCameraEventBundle): MutableList<String>
}

data class PendingCameraEventBundleVersionRangeRule(var min: Int, var max: Int) : PendingCameraEventBundleRule {
    override fun check(item: PendingCameraEventBundle): MutableList<String> {
        val value = item.version
        if (value < min || value > max) {
            return mutableListOf<String>("version $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingCameraEventBundleGraceDaysRangeRule(
    var min: Int,
    var max: Int,
) : PendingCameraEventBundleRule {
    override fun check(item: PendingCameraEventBundle): MutableList<String> {
        val value = item.graceDays
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("grace days $value is below $min")
        }
        if (value > max) {
            problems.add("grace days $value is above $max")
        }
        return problems
    }
}

data class PendingCameraEventBundleRouteCodeLengthRule(var maxLength: Int) : PendingCameraEventBundleRule {
    override fun check(item: PendingCameraEventBundle): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingCameraEventBundleAllowedTitleRule(
    var allowed: MutableList<String>,
) : PendingCameraEventBundleRule {
    override fun check(item: PendingCameraEventBundle): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class PendingCameraEventBundleRequiredSourceRule(var message: String) : PendingCameraEventBundleRule {
    override fun check(item: PendingCameraEventBundle): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending camera event bundle. */
data class PendingCameraEventBundleValidator(var rules: MutableList<PendingCameraEventBundleRule>) {
    fun validate(item: PendingCameraEventBundle): MutableList<String> {
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

    fun requireValid(item: PendingCameraEventBundle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingCameraEventBundle>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingCameraEventBundleValidator(): PendingCameraEventBundleValidator {
    return PendingCameraEventBundleValidator(
        mutableListOf<PendingCameraEventBundleRule>(
            PendingCameraEventBundleVersionRangeRule(16, 80),
            PendingCameraEventBundleGraceDaysRangeRule(120, 120),
            PendingCameraEventBundleRouteCodeLengthRule(1000),
            PendingCameraEventBundleAllowedTitleRule(mutableListOf<String>("seasonal", "adaptive")),
            PendingCameraEventBundleRequiredSourceRule("pending camera event bundle needs this field"),
        ),
    )
}
