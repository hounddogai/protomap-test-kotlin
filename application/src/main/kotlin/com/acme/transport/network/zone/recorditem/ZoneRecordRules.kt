package com.acme.transport.network.zone.recorditem

/** A rule a zone record must pass. Check returns its problems, or none. */
interface ZoneRecordRule {
    fun check(item: ZoneRecord): MutableList<String>
}

data class ZoneRecordRequiredNameRule(var message: String) : ZoneRecordRule {
    override fun check(item: ZoneRecord): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneRecordRequiredTimezoneRule(var message: String) : ZoneRecordRule {
    override fun check(item: ZoneRecord): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ZoneRecordDurationMinutesRangeRule(var min: Int, var max: Int) : ZoneRecordRule {
    override fun check(item: ZoneRecord): MutableList<String> {
        val value = item.durationMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("duration minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneRecordCodePrefixRule(var prefix: String) : ZoneRecordRule {
    override fun check(item: ZoneRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ZoneRecordExpiresAtRangeRule(var min: Int, var max: Int) : ZoneRecordRule {
    override fun check(item: ZoneRecord): MutableList<String> {
        val value = item.expiresAt
        if (value < min || value > max) {
            return mutableListOf<String>("expires at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ZoneRecordRatioRangeRule(var min: Double, var max: Double) : ZoneRecordRule {
    override fun check(item: ZoneRecord): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a zone record. */
data class ZoneRecordValidator(var rules: MutableList<ZoneRecordRule>) {
    fun validate(item: ZoneRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ZoneRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ZoneRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultZoneRecordValidator(): ZoneRecordValidator {
    return ZoneRecordValidator(
        mutableListOf<ZoneRecordRule>(
            ZoneRecordRequiredNameRule("zone record needs this field"),
            ZoneRecordRequiredTimezoneRule("zone record needs a value"),
            ZoneRecordDurationMinutesRangeRule(255, 86400),
            ZoneRecordCodePrefixRule("X"),
            ZoneRecordExpiresAtRangeRule(80, 255),
            ZoneRecordRatioRangeRule(9.8, 1613.7),
        ),
    )
}
