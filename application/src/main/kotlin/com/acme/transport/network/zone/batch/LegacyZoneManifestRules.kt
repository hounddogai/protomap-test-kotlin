package com.acme.transport.network.zone.batch

/** A rule a legacy zone manifest must pass. Check returns its problems, or none. */
interface LegacyZoneManifestRule {
    fun check(item: LegacyZoneManifest): MutableList<String>
}

data class LegacyZoneManifestSequenceRangeRule(var min: Int, var max: Int) : LegacyZoneManifestRule {
    override fun check(item: LegacyZoneManifest): MutableList<String> {
        val value = item.sequence
        if (value < min || value > max) {
            return mutableListOf<String>("sequence $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneManifestRequiredLabelRule(var message: String) : LegacyZoneManifestRule {
    override fun check(item: LegacyZoneManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyZoneManifestWeightRangeRule(var min: Double, var max: Double) : LegacyZoneManifestRule {
    override fun check(item: LegacyZoneManifest): MutableList<String> {
        val value = item.weight
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("weight $value is below $min")
        }
        if (value > max) {
            problems.add("weight $value is above $max")
        }
        return problems
    }
}

data class LegacyZoneManifestCreatedAtRangeRule(var min: Int, var max: Int) : LegacyZoneManifestRule {
    override fun check(item: LegacyZoneManifest): MutableList<String> {
        val value = item.createdAt
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("created at $value is below $min")
        }
        if (value > max) {
            problems.add("created at $value is above $max")
        }
        return problems
    }
}

data class LegacyZoneManifestRequiredExternalRefRule(var message: String) : LegacyZoneManifestRule {
    override fun check(item: LegacyZoneManifest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy zone manifest. */
data class LegacyZoneManifestValidator(var rules: MutableList<LegacyZoneManifestRule>) {
    fun validate(item: LegacyZoneManifest): MutableList<String> {
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

    fun requireValid(item: LegacyZoneManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyZoneManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyZoneManifestValidator(): LegacyZoneManifestValidator {
    return LegacyZoneManifestValidator(
        mutableListOf<LegacyZoneManifestRule>(
            LegacyZoneManifestSequenceRangeRule(255, 1000),
            LegacyZoneManifestRequiredLabelRule("legacy zone manifest needs this field"),
            LegacyZoneManifestWeightRangeRule(4.6, 2978.5),
            LegacyZoneManifestCreatedAtRangeRule(1000, 86400),
            LegacyZoneManifestRequiredExternalRefRule("legacy zone manifest needs a value"),
        ),
    )
}
