package com.acme.transport.network.transittime.recorditem

/** A rule a transit time rollup must pass. Check returns its problems, or none. */
interface TransitTimeRollupRule {
    fun check(item: TransitTimeRollup): MutableList<String>
}

data class TransitTimeRollupCodePrefixRule(var prefix: String) : TransitTimeRollupRule {
    override fun check(item: TransitTimeRollup): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeRollupUpdatedAtRangeRule(var min: Int, var max: Int) : TransitTimeRollupRule {
    override fun check(item: TransitTimeRollup): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeRollupOdometerRangeRule(var min: Int, var max: Int) : TransitTimeRollupRule {
    override fun check(item: TransitTimeRollup): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeRollupEtaMinutesRangeRule(var min: Int, var max: Int) : TransitTimeRollupRule {
    override fun check(item: TransitTimeRollup): MutableList<String> {
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

data class TransitTimeRollupLabelLengthRule(var maxLength: Int) : TransitTimeRollupRule {
    override fun check(item: TransitTimeRollup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TransitTimeRollupRequiredLabelRule(var message: String) : TransitTimeRollupRule {
    override fun check(item: TransitTimeRollup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a transit time rollup. */
data class TransitTimeRollupValidator(var rules: MutableList<TransitTimeRollupRule>) {
    fun validate(item: TransitTimeRollup): MutableList<String> {
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

    fun requireValid(item: TransitTimeRollup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TransitTimeRollup>): MutableMap<String, MutableList<String>> {
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

fun defaultTransitTimeRollupValidator(): TransitTimeRollupValidator {
    return TransitTimeRollupValidator(
        mutableListOf<TransitTimeRollupRule>(
            TransitTimeRollupCodePrefixRule("REF"),
            TransitTimeRollupUpdatedAtRangeRule(16, 1000),
            TransitTimeRollupOdometerRangeRule(255, 255),
            TransitTimeRollupEtaMinutesRangeRule(32, 80),
            TransitTimeRollupLabelLengthRule(80),
            TransitTimeRollupRequiredLabelRule("transit time rollup needs a value"),
        ),
    )
}
