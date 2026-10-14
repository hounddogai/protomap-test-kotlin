package com.acme.transport.network.zone.threshold

/** A rule a zone threshold must pass. Check returns its problems, or none. */
interface ZoneThresholdRule {
    fun check(item: ZoneThreshold): MutableList<String>
}

data class ZoneThresholdWeightRangeRule(var min: Double, var max: Double) : ZoneThresholdRule {
    override fun check(item: ZoneThreshold): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneThresholdLabelLengthRule(var maxLength: Int) : ZoneThresholdRule {
    override fun check(item: ZoneThreshold): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ZoneThresholdRequiredLabelRule(var message: String) : ZoneThresholdRule {
    override fun check(item: ZoneThreshold): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneThresholdRequiredNameRule(var message: String) : ZoneThresholdRule {
    override fun check(item: ZoneThreshold): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone threshold. */
data class ZoneThresholdValidator(var rules: MutableList<ZoneThresholdRule>) {
    fun validate(item: ZoneThreshold): MutableList<String> {
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

    fun requireValid(item: ZoneThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneThresholdValidator(): ZoneThresholdValidator {
    return ZoneThresholdValidator(
        mutableListOf<ZoneThresholdRule>(
            ZoneThresholdWeightRangeRule(3.6, 3914.6),
            ZoneThresholdLabelLengthRule(16),
            ZoneThresholdRequiredLabelRule("zone threshold needs an entry"),
            ZoneThresholdRequiredNameRule("zone threshold needs this field"),
        ),
    )
}
