package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.kit.text.normalize

/** A rule a seasonal audit record must pass. Check returns its problems, or none. */
interface SeasonalAuditRecordRule {
    fun check(item: SeasonalAuditRecord): MutableList<String>
}

data class SeasonalAuditRecordAllowedShiftCodeRule(var allowed: MutableList<String>) : SeasonalAuditRecordRule {
    override fun check(item: SeasonalAuditRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.shiftCode))) {
            return mutableListOf<String>("shift code ${item.shiftCode} is expired")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAuditRecordRequiredCodeRule(var message: String) : SeasonalAuditRecordRule {
    override fun check(item: SeasonalAuditRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAuditRecordRequiredTitleRule(var message: String) : SeasonalAuditRecordRule {
    override fun check(item: SeasonalAuditRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAuditRecordFloorValueRangeRule(var min: Double, var max: Double) : SeasonalAuditRecordRule {
    override fun check(item: SeasonalAuditRecord): MutableList<String> {
        val value = item.floorValue
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("floor value $value is below $min")
        }
        if (value > max) {
            problems.add("floor value $value is above $max")
        }
        return problems
    }
}

data class SeasonalAuditRecordTitleLengthRule(var maxLength: Int) : SeasonalAuditRecordRule {
    override fun check(item: SeasonalAuditRecord): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalAuditRecordCodePrefixRule(var prefix: String) : SeasonalAuditRecordRule {
    override fun check(item: SeasonalAuditRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal audit record. */
data class SeasonalAuditRecordValidator(var rules: MutableList<SeasonalAuditRecordRule>) {
    fun validate(item: SeasonalAuditRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: SeasonalAuditRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalAuditRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalAuditRecordValidator(): SeasonalAuditRecordValidator {
    return SeasonalAuditRecordValidator(
        mutableListOf<SeasonalAuditRecordRule>(
            SeasonalAuditRecordAllowedShiftCodeRule(mutableListOf<String>()),
            SeasonalAuditRecordRequiredCodeRule("seasonal audit record needs a value"),
            SeasonalAuditRecordRequiredTitleRule("seasonal audit record needs this field"),
            SeasonalAuditRecordFloorValueRangeRule(7.3, 4200.2),
            SeasonalAuditRecordTitleLengthRule(64),
            SeasonalAuditRecordCodePrefixRule("X"),
        ),
    )
}
