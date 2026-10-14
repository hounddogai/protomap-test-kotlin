package com.acme.transport.lastmile.parcel.metric

/** A rule a manual parcel metric must pass. Check returns its problems, or none. */
interface ManualParcelMetricRule {
    fun check(item: ManualParcelMetric): MutableList<String>
}

data class ManualParcelMetricRequiredCodeRule(var message: String) : ManualParcelMetricRule {
    override fun check(item: ManualParcelMetric): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelMetricShiftCodeLengthRule(var maxLength: Int) : ManualParcelMetricRule {
    override fun check(item: ManualParcelMetric): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelMetricCodePrefixRule(var prefix: String) : ManualParcelMetricRule {
    override fun check(item: ManualParcelMetric): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelMetricRequiredNameRule(var message: String) : ManualParcelMetricRule {
    override fun check(item: ManualParcelMetric): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualParcelMetricCodeLengthRule(var maxLength: Int) : ManualParcelMetricRule {
    override fun check(item: ManualParcelMetric): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual parcel metric. */
data class ManualParcelMetricValidator(var rules: MutableList<ManualParcelMetricRule>) {
    fun validate(item: ManualParcelMetric): MutableList<String> {
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

    fun requireValid(item: ManualParcelMetric) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualParcelMetric>): MutableMap<String, MutableList<String>> {
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

fun defaultManualParcelMetricValidator(): ManualParcelMetricValidator {
    return ManualParcelMetricValidator(
        mutableListOf<ManualParcelMetricRule>(
            ManualParcelMetricRequiredCodeRule("manual parcel metric needs a value"),
            ManualParcelMetricShiftCodeLengthRule(120),
            ManualParcelMetricCodePrefixRule("Q"),
            ManualParcelMetricRequiredNameRule("manual parcel metric needs an entry"),
            ManualParcelMetricCodeLengthRule(64),
        ),
    )
}
