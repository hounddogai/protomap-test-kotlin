package com.acme.transport.network.node.nodesnapshot

/** A rule a legacy node metric must pass. Check returns its problems, or none. */
interface LegacyNodeMetricRule {
    fun check(item: LegacyNodeMetric): MutableList<String>
}

data class LegacyNodeMetricRequiredPhoneRule(var message: String) : LegacyNodeMetricRule {
    override fun check(item: LegacyNodeMetric): MutableList<String> {
        if (item.phone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyNodeMetricRequiredLabelRule(var message: String) : LegacyNodeMetricRule {
    override fun check(item: LegacyNodeMetric): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyNodeMetricRequiredCodeRule(var message: String) : LegacyNodeMetricRule {
    override fun check(item: LegacyNodeMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyNodeMetricMarginRangeRule(var min: Double, var max: Double) : LegacyNodeMetricRule {
    override fun check(item: LegacyNodeMetric): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy node metric. */
data class LegacyNodeMetricValidator(var rules: MutableList<LegacyNodeMetricRule>) {
    fun validate(item: LegacyNodeMetric): MutableList<String> {
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

    fun requireValid(item: LegacyNodeMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyNodeMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyNodeMetricValidator(): LegacyNodeMetricValidator {
    return LegacyNodeMetricValidator(
        mutableListOf<LegacyNodeMetricRule>(
            LegacyNodeMetricRequiredPhoneRule("legacy node metric needs this field"),
            LegacyNodeMetricRequiredLabelRule("legacy node metric needs this field"),
            LegacyNodeMetricRequiredCodeRule("legacy node metric needs an entry"),
            LegacyNodeMetricMarginRangeRule(7.1, 1832.6),
        ),
    )
}
