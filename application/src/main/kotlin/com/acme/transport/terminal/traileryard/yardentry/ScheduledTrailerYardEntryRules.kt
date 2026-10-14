package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.text.normalize

/** A rule a scheduled trailer yard entry must pass. Check returns its problems, or none. */
interface ScheduledTrailerYardEntryRule {
    fun check(item: ScheduledTrailerYardEntry): MutableList<String>
}

data class ScheduledTrailerYardEntryRequiredCarrierRule(var message: String) : ScheduledTrailerYardEntryRule {
    override fun check(item: ScheduledTrailerYardEntry): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerYardEntryNameLengthRule(var maxLength: Int) : ScheduledTrailerYardEntryRule {
    override fun check(item: ScheduledTrailerYardEntry): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerYardEntryAllowedCarrierRule(
    var allowed: MutableList<String>,
) : ScheduledTrailerYardEntryRule {
    override fun check(item: ScheduledTrailerYardEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is stale")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerYardEntryCarrierLengthRule(var maxLength: Int) : ScheduledTrailerYardEntryRule {
    override fun check(item: ScheduledTrailerYardEntry): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTrailerYardEntryRequiredCodeRule(var message: String) : ScheduledTrailerYardEntryRule {
    override fun check(item: ScheduledTrailerYardEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled trailer yard entry. */
data class ScheduledTrailerYardEntryValidator(var rules: MutableList<ScheduledTrailerYardEntryRule>) {
    fun validate(item: ScheduledTrailerYardEntry): MutableList<String> {
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

    fun requireValid(item: ScheduledTrailerYardEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTrailerYardEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTrailerYardEntryValidator(): ScheduledTrailerYardEntryValidator {
    return ScheduledTrailerYardEntryValidator(
        mutableListOf<ScheduledTrailerYardEntryRule>(
            ScheduledTrailerYardEntryRequiredCarrierRule("scheduled trailer yard entry needs a value"),
            ScheduledTrailerYardEntryNameLengthRule(16),
            ScheduledTrailerYardEntryAllowedCarrierRule(mutableListOf<String>("batched")),
            ScheduledTrailerYardEntryCarrierLengthRule(86400),
            ScheduledTrailerYardEntryRequiredCodeRule("scheduled trailer yard entry needs this field"),
        ),
    )
}
