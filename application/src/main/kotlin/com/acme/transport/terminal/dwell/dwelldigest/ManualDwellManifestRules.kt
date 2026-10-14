package com.acme.transport.terminal.dwell.dwelldigest

/** A rule a manual dwell manifest must pass. Check returns its problems, or none. */
interface ManualDwellManifestRule {
    fun check(item: ManualDwellManifest): MutableList<String>
}

data class ManualDwellManifestCodePrefixRule(var prefix: String) : ManualDwellManifestRule {
    override fun check(item: ManualDwellManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ManualDwellManifestFuelLitersRangeRule(var min: Double, var max: Double) : ManualDwellManifestRule {
    override fun check(item: ManualDwellManifest): MutableList<String> {
        val value = item.fuelLiters
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("fuel liters $value is below $min")
        }
        if (value > max) {
            problems.add("fuel liters $value is above $max")
        }
        return problems
    }
}

data class ManualDwellManifestRequiredCodeRule(var message: String) : ManualDwellManifestRule {
    override fun check(item: ManualDwellManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual dwell manifest. */
data class ManualDwellManifestValidator(var rules: MutableList<ManualDwellManifestRule>) {
    fun validate(item: ManualDwellManifest): MutableList<String> {
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

    fun requireValid(item: ManualDwellManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualDwellManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultManualDwellManifestValidator(): ManualDwellManifestValidator {
    return ManualDwellManifestValidator(
        mutableListOf<ManualDwellManifestRule>(
            ManualDwellManifestCodePrefixRule("OPS"),
            ManualDwellManifestFuelLitersRangeRule(5.9, 1101.4),
            ManualDwellManifestRequiredCodeRule("manual dwell manifest needs a value"),
        ),
    )
}
