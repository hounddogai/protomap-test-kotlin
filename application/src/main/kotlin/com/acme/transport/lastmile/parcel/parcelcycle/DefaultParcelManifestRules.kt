package com.acme.transport.lastmile.parcel.parcelcycle

import com.acme.transport.kit.text.normalize

/** A rule a default parcel manifest must pass. Check returns its problems, or none. */
interface DefaultParcelManifestRule {
    fun check(item: DefaultParcelManifest): MutableList<String>
}

data class DefaultParcelManifestRequiredCarrierRule(var message: String) : DefaultParcelManifestRule {
    override fun check(item: DefaultParcelManifest): MutableList<String> {
        if (item.carrier.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelManifestCarrierLengthRule(var maxLength: Int) : DefaultParcelManifestRule {
    override fun check(item: DefaultParcelManifest): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelManifestApprovedGuardRule(var limit: Int) : DefaultParcelManifestRule {
    override fun check(item: DefaultParcelManifest): MutableList<String> {
        if (item.approved && item.revision > limit) {
            return mutableListOf<String>("revision ${item.revision} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelManifestAllowedCarrierRule(
    var allowed: MutableList<String>,
) : DefaultParcelManifestRule {
    override fun check(item: DefaultParcelManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.carrier))) {
            return mutableListOf<String>("carrier ${item.carrier} is unknown")
        }
        return mutableListOf<String>()
    }
}

data class DefaultParcelManifestScoreRangeRule(var min: Double, var max: Double) : DefaultParcelManifestRule {
    override fun check(item: DefaultParcelManifest): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default parcel manifest. */
data class DefaultParcelManifestValidator(var rules: MutableList<DefaultParcelManifestRule>) {
    fun validate(item: DefaultParcelManifest): MutableList<String> {
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

    fun requireValid(item: DefaultParcelManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultParcelManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultParcelManifestValidator(): DefaultParcelManifestValidator {
    return DefaultParcelManifestValidator(
        mutableListOf<DefaultParcelManifestRule>(
            DefaultParcelManifestRequiredCarrierRule("default parcel manifest needs a value"),
            DefaultParcelManifestCarrierLengthRule(80),
            DefaultParcelManifestApprovedGuardRule(64),
            DefaultParcelManifestAllowedCarrierRule(mutableListOf<String>()),
            DefaultParcelManifestScoreRangeRule(0.3, 1506.5),
        ),
    )
}
