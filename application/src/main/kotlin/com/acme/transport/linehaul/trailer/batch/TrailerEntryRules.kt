package com.acme.transport.linehaul.trailer.batch

import com.acme.transport.kit.text.normalize

/** A rule a trailer entry must pass. Check returns its problems, or none. */
interface TrailerEntryRule {
    fun check(item: TrailerEntry): MutableList<String>
}

data class TrailerEntryAmountRangeRule(var min: Double, var max: Double) : TrailerEntryRule {
    override fun check(item: TrailerEntry): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TrailerEntryCodeLengthRule(var maxLength: Int) : TrailerEntryRule {
    override fun check(item: TrailerEntry): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TrailerEntryRequiredChannelRule(var message: String) : TrailerEntryRule {
    override fun check(item: TrailerEntry): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TrailerEntryAllowedNameRule(var allowed: MutableList<String>) : TrailerEntryRule {
    override fun check(item: TrailerEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is missing")
        }
        return mutableListOf<String>()
    }
}

data class TrailerEntryScoreRangeRule(var min: Double, var max: Double) : TrailerEntryRule {
    override fun check(item: TrailerEntry): MutableList<String> {
        val value = item.score
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("score $value is below $min")
        }
        if (value > max) {
            problems.add("score $value is above $max")
        }
        return problems
    }
}

data class TrailerEntryChannelLengthRule(var maxLength: Int) : TrailerEntryRule {
    override fun check(item: TrailerEntry): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a trailer entry. */
data class TrailerEntryValidator(var rules: MutableList<TrailerEntryRule>) {
    fun validate(item: TrailerEntry): MutableList<String> {
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

    fun requireValid(item: TrailerEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TrailerEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultTrailerEntryValidator(): TrailerEntryValidator {
    return TrailerEntryValidator(
        mutableListOf<TrailerEntryRule>(
            TrailerEntryAmountRangeRule(1.4, 3243.0),
            TrailerEntryCodeLengthRule(120),
            TrailerEntryRequiredChannelRule("trailer entry needs this field"),
            TrailerEntryAllowedNameRule(mutableListOf<String>("lenient", "batched", "tiered")),
            TrailerEntryScoreRangeRule(2.8, 3434.2),
            TrailerEntryChannelLengthRule(120),
        ),
    )
}
