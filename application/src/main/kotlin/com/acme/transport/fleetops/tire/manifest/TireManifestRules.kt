package com.acme.transport.fleetops.tire.manifest

/** A rule a tire manifest must pass. Check returns its problems, or none. */
interface TireManifestRule {
    fun check(item: TireManifest): MutableList<String>
}

data class TireManifestCodeLengthRule(var maxLength: Int) : TireManifestRule {
    override fun check(item: TireManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TireManifestRequiredUnitRule(var message: String) : TireManifestRule {
    override fun check(item: TireManifest): MutableList<String> {
        if (item.unit.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TireManifestOdometerRangeRule(var min: Int, var max: Int) : TireManifestRule {
    override fun check(item: TireManifest): MutableList<String> {
        val value = item.odometer
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("odometer $value is below $min")
        }
        if (value > max) {
            problems.add("odometer $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tire manifest. */
data class TireManifestValidator(var rules: MutableList<TireManifestRule>) {
    fun validate(item: TireManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TireManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TireManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultTireManifestValidator(): TireManifestValidator {
    return TireManifestValidator(
        mutableListOf<TireManifestRule>(
            TireManifestCodeLengthRule(80),
            TireManifestRequiredUnitRule("tire manifest needs a value"),
            TireManifestOdometerRangeRule(120, 1000),
        ),
    )
}
