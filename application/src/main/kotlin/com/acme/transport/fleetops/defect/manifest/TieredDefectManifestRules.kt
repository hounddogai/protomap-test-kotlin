package com.acme.transport.fleetops.defect.manifest

/** A rule a tiered defect manifest must pass. Check returns its problems, or none. */
interface TieredDefectManifestRule {
    fun check(item: TieredDefectManifest): MutableList<String>
}

data class TieredDefectManifestLocaleLengthRule(var maxLength: Int) : TieredDefectManifestRule {
    override fun check(item: TieredDefectManifest): MutableList<String> {
        val size = item.locale.length
        if (size > maxLength) {
            return mutableListOf<String>("locale is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDefectManifestTitleLengthRule(var maxLength: Int) : TieredDefectManifestRule {
    override fun check(item: TieredDefectManifest): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDefectManifestLimitRangeRule(var min: Int, var max: Int) : TieredDefectManifestRule {
    override fun check(item: TieredDefectManifest): MutableList<String> {
        val value = item.limit
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("limit $value is below $min")
        }
        if (value > max) {
            problems.add("limit $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a tiered defect manifest. */
data class TieredDefectManifestValidator(var rules: MutableList<TieredDefectManifestRule>) {
    fun validate(item: TieredDefectManifest): MutableList<String> {
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

    fun requireValid(item: TieredDefectManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDefectManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDefectManifestValidator(): TieredDefectManifestValidator {
    return TieredDefectManifestValidator(
        mutableListOf<TieredDefectManifestRule>(
            TieredDefectManifestLocaleLengthRule(255),
            TieredDefectManifestTitleLengthRule(32),
            TieredDefectManifestLimitRangeRule(64, 80),
        ),
    )
}
