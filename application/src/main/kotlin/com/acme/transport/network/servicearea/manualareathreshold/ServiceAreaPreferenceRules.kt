package com.acme.transport.network.servicearea.manualareathreshold

/** A rule a service area preference must pass. Check returns its problems, or none. */
interface ServiceAreaPreferenceRule {
    fun check(item: ServiceAreaPreference): MutableList<String>
}

data class ServiceAreaPreferenceLabelLengthRule(var maxLength: Int) : ServiceAreaPreferenceRule {
    override fun check(item: ServiceAreaPreference): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaPreferenceEndsAtRangeRule(var min: Int, var max: Int) : ServiceAreaPreferenceRule {
    override fun check(item: ServiceAreaPreference): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaPreferenceCostRangeRule(var min: Double, var max: Double) : ServiceAreaPreferenceRule {
    override fun check(item: ServiceAreaPreference): MutableList<String> {
        val value = item.cost
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("cost $value is below $min")
        }
        if (value > max) {
            problems.add("cost $value is above $max")
        }
        return problems
    }
}

data class ServiceAreaPreferenceTimezoneLengthRule(var maxLength: Int) : ServiceAreaPreferenceRule {
    override fun check(item: ServiceAreaPreference): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaPreferenceSequenceRangeRule(var min: Int, var max: Int) : ServiceAreaPreferenceRule {
    override fun check(item: ServiceAreaPreference): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaPreferenceCodePrefixRule(var prefix: String) : ServiceAreaPreferenceRule {
    override fun check(item: ServiceAreaPreference): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a service area preference. */
data class ServiceAreaPreferenceValidator(var rules: MutableList<ServiceAreaPreferenceRule>) {
    fun validate(item: ServiceAreaPreference): MutableList<String> {
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

    fun requireValid(item: ServiceAreaPreference) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaPreference>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaPreferenceValidator(): ServiceAreaPreferenceValidator {
    return ServiceAreaPreferenceValidator(
        mutableListOf<ServiceAreaPreferenceRule>(
            ServiceAreaPreferenceLabelLengthRule(64),
            ServiceAreaPreferenceEndsAtRangeRule(64, 86400),
            ServiceAreaPreferenceCostRangeRule(3.6, 3402.2),
            ServiceAreaPreferenceTimezoneLengthRule(255),
            ServiceAreaPreferenceSequenceRangeRule(1000, 86400),
            ServiceAreaPreferenceCodePrefixRule("REF"),
        ),
    )
}
