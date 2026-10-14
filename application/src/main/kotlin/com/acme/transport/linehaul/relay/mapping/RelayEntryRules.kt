package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.text.normalize

/** A rule a relay entry must pass. Check returns its problems, or none. */
interface RelayEntryRule {
    fun check(item: RelayEntry): MutableList<String>
}

data class RelayEntryRequiredCodeRule(var message: String) : RelayEntryRule {
    override fun check(item: RelayEntry): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RelayEntryVehicleIdLengthRule(var maxLength: Int) : RelayEntryRule {
    override fun check(item: RelayEntry): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RelayEntryAllowedVehicleIdRule(var allowed: MutableList<String>) : RelayEntryRule {
    override fun check(item: RelayEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.vehicleId))) {
            return mutableListOf<String>("vehicle id ${item.vehicleId} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a relay entry. */
data class RelayEntryValidator(var rules: MutableList<RelayEntryRule>) {
    fun validate(item: RelayEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RelayEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RelayEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultRelayEntryValidator(): RelayEntryValidator {
    return RelayEntryValidator(
        mutableListOf<RelayEntryRule>(
            RelayEntryRequiredCodeRule("relay entry needs this field"),
            RelayEntryVehicleIdLengthRule(1000),
            RelayEntryAllowedVehicleIdRule(mutableListOf<String>("pending")),
        ),
    )
}
