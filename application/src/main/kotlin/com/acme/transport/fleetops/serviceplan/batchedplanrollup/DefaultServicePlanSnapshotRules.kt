package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import com.acme.transport.kit.text.normalize

/** A rule a default service plan snapshot must pass. Check returns its problems, or none. */
interface DefaultServicePlanSnapshotRule {
    fun check(item: DefaultServicePlanSnapshot): MutableList<String>
}

data class DefaultServicePlanSnapshotRequiredCodeRule(var message: String) : DefaultServicePlanSnapshotRule {
    override fun check(item: DefaultServicePlanSnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultServicePlanSnapshotCodeLengthRule(var maxLength: Int) : DefaultServicePlanSnapshotRule {
    override fun check(item: DefaultServicePlanSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultServicePlanSnapshotEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : DefaultServicePlanSnapshotRule {
    override fun check(item: DefaultServicePlanSnapshot): MutableList<String> {
        val value = item.etaMinutes
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("eta minutes $value is below $min")
        }
        if (value > max) {
            problems.add("eta minutes $value is above $max")
        }
        return problems
    }
}

data class DefaultServicePlanSnapshotAllowedTitleRule(
    var allowed: MutableList<String>,
) : DefaultServicePlanSnapshotRule {
    override fun check(item: DefaultServicePlanSnapshot): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is expired")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default service plan snapshot. */
data class DefaultServicePlanSnapshotValidator(var rules: MutableList<DefaultServicePlanSnapshotRule>) {
    fun validate(item: DefaultServicePlanSnapshot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultServicePlanSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultServicePlanSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultServicePlanSnapshotValidator(): DefaultServicePlanSnapshotValidator {
    return DefaultServicePlanSnapshotValidator(
        mutableListOf<DefaultServicePlanSnapshotRule>(
            DefaultServicePlanSnapshotRequiredCodeRule("default service plan snapshot needs this field"),
            DefaultServicePlanSnapshotCodeLengthRule(64),
            DefaultServicePlanSnapshotEtaMinutesRangeRule(120, 255),
            DefaultServicePlanSnapshotAllowedTitleRule(mutableListOf<String>("regional", "lenient")),
        ),
    )
}
