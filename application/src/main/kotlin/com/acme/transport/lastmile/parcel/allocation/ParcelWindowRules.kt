package com.acme.transport.lastmile.parcel.allocation

/** A rule a parcel window must pass. Check returns its problems, or none. */
interface ParcelWindowRule {
    fun check(item: ParcelWindow): MutableList<String>
}

data class ParcelWindowRequiredVehicleIdRule(var message: String) : ParcelWindowRule {
    override fun check(item: ParcelWindow): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelWindowRateRangeRule(var min: Double, var max: Double) : ParcelWindowRule {
    override fun check(item: ParcelWindow): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class ParcelWindowEndsAtRangeRule(var min: Int, var max: Int) : ParcelWindowRule {
    override fun check(item: ParcelWindow): MutableList<String> {
        val value = item.endsAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ends at $value is below $min")
        }
        if (value > max) {
            problems.add("ends at $value is above $max")
        }
        return problems
    }
}

data class ParcelWindowNameLengthRule(var maxLength: Int) : ParcelWindowRule {
    override fun check(item: ParcelWindow): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelWindowDriverIdLengthRule(var maxLength: Int) : ParcelWindowRule {
    override fun check(item: ParcelWindow): MutableList<String> {
        val size = item.driverId.length
        if (size > maxLength) {
            return mutableListOf<String>("driver id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel window. */
data class ParcelWindowValidator(var rules: MutableList<ParcelWindowRule>) {
    fun validate(item: ParcelWindow): MutableList<String> {
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

    fun requireValid(item: ParcelWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelWindowValidator(): ParcelWindowValidator {
    return ParcelWindowValidator(
        mutableListOf<ParcelWindowRule>(
            ParcelWindowRequiredVehicleIdRule("parcel window needs this field"),
            ParcelWindowRateRangeRule(5.0, 87.2),
            ParcelWindowEndsAtRangeRule(64, 120),
            ParcelWindowNameLengthRule(120),
            ParcelWindowDriverIdLengthRule(64),
        ),
    )
}
