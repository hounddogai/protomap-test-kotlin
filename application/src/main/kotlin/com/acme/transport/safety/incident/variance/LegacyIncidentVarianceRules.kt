package com.acme.transport.safety.incident.variance

/** A rule a legacy incident variance must pass. Check returns its problems, or none. */
interface LegacyIncidentVarianceRule {
    fun check(item: LegacyIncidentVariance): MutableList<String>
}

data class LegacyIncidentVariancePalletsRangeRule(var min: Int, var max: Int) : LegacyIncidentVarianceRule {
    override fun check(item: LegacyIncidentVariance): MutableList<String> {
        val value = item.pallets
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("pallets $value is below $min")
        }
        if (value > max) {
            problems.add("pallets $value is above $max")
        }
        return problems
    }
}

data class LegacyIncidentVarianceQuantityRangeRule(var min: Int, var max: Int) : LegacyIncidentVarianceRule {
    override fun check(item: LegacyIncidentVariance): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

data class LegacyIncidentVarianceRequiredLabelRule(var message: String) : LegacyIncidentVarianceRule {
    override fun check(item: LegacyIncidentVariance): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentVarianceTitleLengthRule(var maxLength: Int) : LegacyIncidentVarianceRule {
    override fun check(item: LegacyIncidentVariance): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentVarianceRequiredTitleRule(var message: String) : LegacyIncidentVarianceRule {
    override fun check(item: LegacyIncidentVariance): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyIncidentVarianceRequiredVehicleIdRule(var message: String) : LegacyIncidentVarianceRule {
    override fun check(item: LegacyIncidentVariance): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy incident variance. */
data class LegacyIncidentVarianceValidator(var rules: MutableList<LegacyIncidentVarianceRule>) {
    fun validate(item: LegacyIncidentVariance): MutableList<String> {
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

    fun requireValid(item: LegacyIncidentVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyIncidentVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyIncidentVarianceValidator(): LegacyIncidentVarianceValidator {
    return LegacyIncidentVarianceValidator(
        mutableListOf<LegacyIncidentVarianceRule>(
            LegacyIncidentVariancePalletsRangeRule(32, 64),
            LegacyIncidentVarianceQuantityRangeRule(120, 86400),
            LegacyIncidentVarianceRequiredLabelRule("legacy incident variance needs an entry"),
            LegacyIncidentVarianceTitleLengthRule(32),
            LegacyIncidentVarianceRequiredTitleRule("legacy incident variance needs this field"),
            LegacyIncidentVarianceRequiredVehicleIdRule("legacy incident variance needs a value"),
        ),
    )
}
