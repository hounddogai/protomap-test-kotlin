package com.acme.transport.fleetops.defect.defectmapping

/** A rule a weighted defect manifest must pass. Check returns its problems, or none. */
interface WeightedDefectManifestRule {
    fun check(item: WeightedDefectManifest): MutableList<String>
}

data class WeightedDefectManifestQuantityRangeRule(var min: Int, var max: Int) : WeightedDefectManifestRule {
    override fun check(item: WeightedDefectManifest): MutableList<String> {
        val value = item.quantity
        if (value < min || value > max) {
            return mutableListOf<String>("quantity $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDefectManifestRequiredDockRule(var message: String) : WeightedDefectManifestRule {
    override fun check(item: WeightedDefectManifest): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDefectManifestCodePrefixRule(var prefix: String) : WeightedDefectManifestRule {
    override fun check(item: WeightedDefectManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted defect manifest. */
data class WeightedDefectManifestValidator(var rules: MutableList<WeightedDefectManifestRule>) {
    fun validate(item: WeightedDefectManifest): MutableList<String> {
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

    fun requireValid(item: WeightedDefectManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDefectManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDefectManifestValidator(): WeightedDefectManifestValidator {
    return WeightedDefectManifestValidator(
        mutableListOf<WeightedDefectManifestRule>(
            WeightedDefectManifestQuantityRangeRule(64, 3600),
            WeightedDefectManifestRequiredDockRule("weighted defect manifest needs a value"),
            WeightedDefectManifestCodePrefixRule("Q"),
        ),
    )
}
