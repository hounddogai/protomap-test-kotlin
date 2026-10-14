package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.text.normalize

/** A rule a ev charge record must pass. Check returns its problems, or none. */
interface EvChargeRecordRule {
    fun check(item: EvChargeRecord): MutableList<String>
}

data class EvChargeRecordRequiredCodeRule(var message: String) : EvChargeRecordRule {
    override fun check(item: EvChargeRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class EvChargeRecordCodeLengthRule(var maxLength: Int) : EvChargeRecordRule {
    override fun check(item: EvChargeRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeRecordAllowedCodeRule(var allowed: MutableList<String>) : EvChargeRecordRule {
    override fun check(item: EvChargeRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeRecordRequiredShiftCodeRule(var message: String) : EvChargeRecordRule {
    override fun check(item: EvChargeRecord): MutableList<String> {
        if (item.shiftCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge record. */
data class EvChargeRecordValidator(var rules: MutableList<EvChargeRecordRule>) {
    fun validate(item: EvChargeRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: EvChargeRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeRecordValidator(): EvChargeRecordValidator {
    return EvChargeRecordValidator(
        mutableListOf<EvChargeRecordRule>(
            EvChargeRecordRequiredCodeRule("ev charge record needs a value"),
            EvChargeRecordCodeLengthRule(32),
            EvChargeRecordAllowedCodeRule(mutableListOf<String>("regional", "scheduled")),
            EvChargeRecordRequiredShiftCodeRule("ev charge record needs a value"),
        ),
    )
}
