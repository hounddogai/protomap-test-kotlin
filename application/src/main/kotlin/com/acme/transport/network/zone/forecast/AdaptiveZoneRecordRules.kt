package com.acme.transport.network.zone.forecast

/** A rule a adaptive zone record must pass. Check returns its problems, or none. */
interface AdaptiveZoneRecordRule {
    fun check(item: AdaptiveZoneRecord): MutableList<String>
}

data class AdaptiveZoneRecordCodePrefixRule(var prefix: String) : AdaptiveZoneRecordRule {
    override fun check(item: AdaptiveZoneRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveZoneRecordRequiredCarrierRule(var message: String) : AdaptiveZoneRecordRule {
    override fun check(item: AdaptiveZoneRecord): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveZoneRecordRequiredNameRule(var message: String) : AdaptiveZoneRecordRule {
    override fun check(item: AdaptiveZoneRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class AdaptiveZoneRecordMaximumRangeRule(var min: Double, var max: Double) : AdaptiveZoneRecordRule {
    override fun check(item: AdaptiveZoneRecord): MutableList<String> {
        val value = item.maximum
        if (value < min || value > max) {
            return mutableListOf<String>("maximum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a adaptive zone record. */
data class AdaptiveZoneRecordValidator(var rules: MutableList<AdaptiveZoneRecordRule>) {
    fun validate(item: AdaptiveZoneRecord): MutableList<String> {
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

    fun requireValid(item: AdaptiveZoneRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<AdaptiveZoneRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultAdaptiveZoneRecordValidator(): AdaptiveZoneRecordValidator {
    return AdaptiveZoneRecordValidator(
        mutableListOf<AdaptiveZoneRecordRule>(
            AdaptiveZoneRecordCodePrefixRule("X"),
            AdaptiveZoneRecordRequiredCarrierRule("adaptive zone record needs an entry"),
            AdaptiveZoneRecordRequiredNameRule("adaptive zone record needs an entry"),
            AdaptiveZoneRecordMaximumRangeRule(6.4, 1758.1),
        ),
    )
}
