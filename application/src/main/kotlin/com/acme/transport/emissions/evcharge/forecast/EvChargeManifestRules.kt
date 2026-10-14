package com.acme.transport.emissions.evcharge.forecast

/** A rule a ev charge manifest must pass. Check returns its problems, or none. */
interface EvChargeManifestRule {
    fun check(item: EvChargeManifest): MutableList<String>
}

data class EvChargeManifestSegmentLengthRule(var maxLength: Int) : EvChargeManifestRule {
    override fun check(item: EvChargeManifest): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeManifestMarginRangeRule(var min: Double, var max: Double) : EvChargeManifestRule {
    override fun check(item: EvChargeManifest): MutableList<String> {
        val value = item.margin
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("margin $value is below $min")
        }
        if (value > max) {
            problems.add("margin $value is above $max")
        }
        return problems
    }
}

data class EvChargeManifestCodeLengthRule(var maxLength: Int) : EvChargeManifestRule {
    override fun check(item: EvChargeManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class EvChargeManifestRequiredDepotRule(var message: String) : EvChargeManifestRule {
    override fun check(item: EvChargeManifest): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a ev charge manifest. */
data class EvChargeManifestValidator(var rules: MutableList<EvChargeManifestRule>) {
    fun validate(item: EvChargeManifest): MutableList<String> {
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

    fun requireValid(item: EvChargeManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<EvChargeManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultEvChargeManifestValidator(): EvChargeManifestValidator {
    return EvChargeManifestValidator(
        mutableListOf<EvChargeManifestRule>(
            EvChargeManifestSegmentLengthRule(32),
            EvChargeManifestMarginRangeRule(0.7, 402.3),
            EvChargeManifestCodeLengthRule(255),
            EvChargeManifestRequiredDepotRule("ev charge manifest needs this field"),
        ),
    )
}
