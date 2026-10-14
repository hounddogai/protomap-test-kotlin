package com.acme.transport.network.zone.pendingzonemapping

/** A rule a strict zone record must pass. Check returns its problems, or none. */
interface StrictZoneRecordRule {
    fun check(item: StrictZoneRecord): MutableList<String>
}

data class StrictZoneRecordRequiredContactEmailRule(var message: String) : StrictZoneRecordRule {
    override fun check(item: StrictZoneRecord): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictZoneRecordVehicleIdLengthRule(var maxLength: Int) : StrictZoneRecordRule {
    override fun check(item: StrictZoneRecord): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictZoneRecordRequiredTitleRule(var message: String) : StrictZoneRecordRule {
    override fun check(item: StrictZoneRecord): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class StrictZoneRecordQuantityRangeRule(var min: Int, var max: Int) : StrictZoneRecordRule {
    override fun check(item: StrictZoneRecord): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict zone record. */
data class StrictZoneRecordValidator(var rules: MutableList<StrictZoneRecordRule>) {
    fun validate(item: StrictZoneRecord): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: StrictZoneRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictZoneRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictZoneRecordValidator(): StrictZoneRecordValidator {
    return StrictZoneRecordValidator(
        mutableListOf<StrictZoneRecordRule>(
            StrictZoneRecordRequiredContactEmailRule("strict zone record needs this field"),
            StrictZoneRecordVehicleIdLengthRule(80),
            StrictZoneRecordRequiredTitleRule("strict zone record needs a value"),
            StrictZoneRecordQuantityRangeRule(64, 3600),
        ),
    )
}
