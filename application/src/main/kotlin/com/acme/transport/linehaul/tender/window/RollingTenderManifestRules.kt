package com.acme.transport.linehaul.tender.window

/** A rule a rolling tender manifest must pass. Check returns its problems, or none. */
interface RollingTenderManifestRule {
    fun check(item: RollingTenderManifest): MutableList<String>
}

data class RollingTenderManifestRequiredLaneRule(var message: String) : RollingTenderManifestRule {
    override fun check(item: RollingTenderManifest): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTenderManifestRequiredLabelRule(var message: String) : RollingTenderManifestRule {
    override fun check(item: RollingTenderManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingTenderManifestLaneLengthRule(var maxLength: Int) : RollingTenderManifestRule {
    override fun check(item: RollingTenderManifest): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling tender manifest. */
data class RollingTenderManifestValidator(var rules: MutableList<RollingTenderManifestRule>) {
    fun validate(item: RollingTenderManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingTenderManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingTenderManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingTenderManifestValidator(): RollingTenderManifestValidator {
    return RollingTenderManifestValidator(
        mutableListOf<RollingTenderManifestRule>(
            RollingTenderManifestRequiredLaneRule("rolling tender manifest needs a value"),
            RollingTenderManifestRequiredLabelRule("rolling tender manifest needs a value"),
            RollingTenderManifestLaneLengthRule(80),
        ),
    )
}
