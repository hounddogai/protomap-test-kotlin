package com.acme.transport.lastmile.parcel.review

/** A rule a weighted parcel threshold must pass. Check returns its problems, or none. */
interface WeightedParcelThresholdRule {
    fun check(item: WeightedParcelThreshold): MutableList<String>
}

data class WeightedParcelThresholdCodeLengthRule(var maxLength: Int) : WeightedParcelThresholdRule {
    override fun check(item: WeightedParcelThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelThresholdRequiredCodeRule(var message: String) : WeightedParcelThresholdRule {
    override fun check(item: WeightedParcelThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelThresholdCodePrefixRule(var prefix: String) : WeightedParcelThresholdRule {
    override fun check(item: WeightedParcelThreshold): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class WeightedParcelThresholdPalletsRangeRule(var min: Int, var max: Int) : WeightedParcelThresholdRule {
    override fun check(item: WeightedParcelThreshold): MutableList<String> {
        val value = item.pallets
        if (value < min || value > max) {
            return mutableListOf<String>("pallets $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted parcel threshold. */
data class WeightedParcelThresholdValidator(var rules: MutableList<WeightedParcelThresholdRule>) {
    fun validate(item: WeightedParcelThreshold): MutableList<String> {
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

    fun requireValid(item: WeightedParcelThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedParcelThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedParcelThresholdValidator(): WeightedParcelThresholdValidator {
    return WeightedParcelThresholdValidator(
        mutableListOf<WeightedParcelThresholdRule>(
            WeightedParcelThresholdCodeLengthRule(64),
            WeightedParcelThresholdRequiredCodeRule("weighted parcel threshold needs an entry"),
            WeightedParcelThresholdCodePrefixRule("Q"),
            WeightedParcelThresholdPalletsRangeRule(32, 86400),
        ),
    )
}
