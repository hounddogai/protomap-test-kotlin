package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

/** A rule a default parcel tier must pass. Check returns its problems, or none. */
interface DefaultParcelTierRule {
    fun check(item: DefaultParcelTier): MutableList<String>
}

data class DefaultParcelTierLabelLengthRule(var maxLength: Int) : DefaultParcelTierRule {
    override fun check(item: DefaultParcelTier): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelTierBatchSizeRangeRule(var min: Int, var max: Int) : DefaultParcelTierRule {
    override fun check(item: DefaultParcelTier): MutableList<String> {
        val value = item.batchSize
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("batch size $value is below $min")
        }
        if (value > max) {
            problems.add("batch size $value is above $max")
        }
        return problems
    }
}

data class DefaultParcelTierDistanceKmRangeRule(var min: Double, var max: Double) : DefaultParcelTierRule {
    override fun check(item: DefaultParcelTier): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelTierCodePrefixRule(var prefix: String) : DefaultParcelTierRule {
    override fun check(item: DefaultParcelTier): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelTierCodeLengthRule(var maxLength: Int) : DefaultParcelTierRule {
    override fun check(item: DefaultParcelTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelTierRequiredLabelRule(var message: String) : DefaultParcelTierRule {
    override fun check(item: DefaultParcelTier): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default parcel tier. */
data class DefaultParcelTierValidator(var rules: MutableList<DefaultParcelTierRule>) {
    fun validate(item: DefaultParcelTier): MutableList<String> {
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

    fun requireValid(item: DefaultParcelTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultParcelTier>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultParcelTierValidator(): DefaultParcelTierValidator {
    return DefaultParcelTierValidator(
        mutableListOf<DefaultParcelTierRule>(
            DefaultParcelTierLabelLengthRule(3600),
            DefaultParcelTierBatchSizeRangeRule(16, 120),
            DefaultParcelTierDistanceKmRangeRule(7.8, 3848.6),
            DefaultParcelTierCodePrefixRule("ACM"),
            DefaultParcelTierCodeLengthRule(120),
            DefaultParcelTierRequiredLabelRule("default parcel tier needs this field"),
        ),
    )
}
