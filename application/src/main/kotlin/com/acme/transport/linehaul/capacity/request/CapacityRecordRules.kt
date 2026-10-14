package com.acme.transport.linehaul.capacity.request

/** A rule a capacity record must pass. Check returns its problems, or none. */
interface CapacityRecordRule {
    fun check(item: CapacityRecord): MutableList<String>
}

data class CapacityRecordVehicleIdLengthRule(var maxLength: Int) : CapacityRecordRule {
    override fun check(item: CapacityRecord): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CapacityRecordCodePrefixRule(var prefix: String) : CapacityRecordRule {
    override fun check(item: CapacityRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CapacityRecordRequiredCodeRule(var message: String) : CapacityRecordRule {
    override fun check(item: CapacityRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capacity record. */
data class CapacityRecordValidator(var rules: MutableList<CapacityRecordRule>) {
    fun validate(item: CapacityRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: CapacityRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CapacityRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultCapacityRecordValidator(): CapacityRecordValidator {
    return CapacityRecordValidator(
        mutableListOf<CapacityRecordRule>(
            CapacityRecordVehicleIdLengthRule(32),
            CapacityRecordCodePrefixRule("Q"),
            CapacityRecordRequiredCodeRule("capacity record needs a value"),
        ),
    )
}
