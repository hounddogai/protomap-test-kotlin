package com.acme.transport.emissions.evcharge.recorditem

/** A rule a capped ev charge record must pass. Check returns its problems, or none. */
interface CappedEvChargeRecordRule {
    fun check(item: CappedEvChargeRecord): MutableList<String>
}

data class CappedEvChargeRecordEndsAtRangeRule(var min: Int, var max: Int) : CappedEvChargeRecordRule {
    override fun check(item: CappedEvChargeRecord): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedEvChargeRecordRouteCodeLengthRule(var maxLength: Int) : CappedEvChargeRecordRule {
    override fun check(item: CappedEvChargeRecord): MutableList<String> {
        val size = item.routeCode.length
        if (size > maxLength) {
            return mutableListOf<String>("route code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedEvChargeRecordCodeLengthRule(var maxLength: Int) : CappedEvChargeRecordRule {
    override fun check(item: CappedEvChargeRecord): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedEvChargeRecordRequiredVehicleIdRule(var message: String) : CappedEvChargeRecordRule {
    override fun check(item: CappedEvChargeRecord): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedEvChargeRecordVehicleIdLengthRule(var maxLength: Int) : CappedEvChargeRecordRule {
    override fun check(item: CappedEvChargeRecord): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CappedEvChargeRecordRequiredCodeRule(var message: String) : CappedEvChargeRecordRule {
    override fun check(item: CappedEvChargeRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped ev charge record. */
data class CappedEvChargeRecordValidator(var rules: MutableList<CappedEvChargeRecordRule>) {
    fun validate(item: CappedEvChargeRecord): MutableList<String> {
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

    fun requireValid(item: CappedEvChargeRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedEvChargeRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedEvChargeRecordValidator(): CappedEvChargeRecordValidator {
    return CappedEvChargeRecordValidator(
        mutableListOf<CappedEvChargeRecordRule>(
            CappedEvChargeRecordEndsAtRangeRule(86400, 86400),
            CappedEvChargeRecordRouteCodeLengthRule(80),
            CappedEvChargeRecordCodeLengthRule(120),
            CappedEvChargeRecordRequiredVehicleIdRule("capped ev charge record needs this field"),
            CappedEvChargeRecordVehicleIdLengthRule(86400),
            CappedEvChargeRecordRequiredCodeRule("capped ev charge record needs a value"),
        ),
    )
}
