package com.acme.transport.fleetops.tire.estimate

import com.acme.transport.kit.text.normalize

/** A rule a tire reservation must pass. Check returns its problems, or none. */
interface TireReservationRule {
    fun check(item: TireReservation): MutableList<String>
}

data class TireReservationCodeLengthRule(var maxLength: Int) : TireReservationRule {
    override fun check(item: TireReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireReservationCodePrefixRule(var prefix: String) : TireReservationRule {
    override fun check(item: TireReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TireReservationRequiredTitleRule(var message: String) : TireReservationRule {
    override fun check(item: TireReservation): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireReservationAllowedTitleRule(var allowed: MutableList<String>) : TireReservationRule {
    override fun check(item: TireReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class TireReservationBatchSizeRangeRule(var min: Int, var max: Int) : TireReservationRule {
    override fun check(item: TireReservation): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TireReservationCostRangeRule(var min: Double, var max: Double) : TireReservationRule {
    override fun check(item: TireReservation): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tire reservation. */
data class TireReservationValidator(var rules: MutableList<TireReservationRule>) {
    fun validate(item: TireReservation): MutableList<String> {
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

    fun requireValid(item: TireReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultTireReservationValidator(): TireReservationValidator {
    return TireReservationValidator(
        mutableListOf<TireReservationRule>(
            TireReservationCodeLengthRule(255),
            TireReservationCodePrefixRule("OPS"),
            TireReservationRequiredTitleRule("tire reservation needs an entry"),
            TireReservationAllowedTitleRule(mutableListOf<String>("seasonal")),
            TireReservationBatchSizeRangeRule(16, 1000),
            TireReservationCostRangeRule(0.4, 3590.8),
        ),
    )
}
