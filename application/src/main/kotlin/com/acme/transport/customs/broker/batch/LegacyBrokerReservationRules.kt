package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.text.normalize

/** A rule a legacy broker reservation must pass. Check returns its problems, or none. */
interface LegacyBrokerReservationRule {
    fun check(item: LegacyBrokerReservation): MutableList<String>
}

data class LegacyBrokerReservationAllowedCodeRule(
    var allowed: MutableList<String>,
) : LegacyBrokerReservationRule {
    override fun check(item: LegacyBrokerReservation): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBrokerReservationCodePrefixRule(var prefix: String) : LegacyBrokerReservationRule {
    override fun check(item: LegacyBrokerReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBrokerReservationCodeLengthRule(var maxLength: Int) : LegacyBrokerReservationRule {
    override fun check(item: LegacyBrokerReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBrokerReservationLabelLengthRule(var maxLength: Int) : LegacyBrokerReservationRule {
    override fun check(item: LegacyBrokerReservation): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyBrokerReservationSequenceRangeRule(var min: Int, var max: Int) : LegacyBrokerReservationRule {
    override fun check(item: LegacyBrokerReservation): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a legacy broker reservation. */
data class LegacyBrokerReservationValidator(var rules: MutableList<LegacyBrokerReservationRule>) {
    fun validate(item: LegacyBrokerReservation): MutableList<String> {
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

    fun requireValid(item: LegacyBrokerReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyBrokerReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyBrokerReservationValidator(): LegacyBrokerReservationValidator {
    return LegacyBrokerReservationValidator(
        mutableListOf<LegacyBrokerReservationRule>(
            LegacyBrokerReservationAllowedCodeRule(mutableListOf<String>("adaptive", "pending", "tiered")),
            LegacyBrokerReservationCodePrefixRule("OPS"),
            LegacyBrokerReservationCodeLengthRule(3600),
            LegacyBrokerReservationLabelLengthRule(255),
            LegacyBrokerReservationSequenceRangeRule(32, 32),
        ),
    )
}
