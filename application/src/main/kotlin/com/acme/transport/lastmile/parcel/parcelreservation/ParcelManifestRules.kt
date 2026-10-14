package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.text.normalize

/** A rule a parcel manifest must pass. Check returns its problems, or none. */
interface ParcelManifestRule {
    fun check(item: ParcelManifest): MutableList<String>
}

data class ParcelManifestAllowedLabelRule(var allowed: MutableList<String>) : ParcelManifestRule {
    override fun check(item: ParcelManifest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is above maximum")
        }
        return mutableListOf<String>()
    }
}

data class ParcelManifestRequiredCodeRule(var message: String) : ParcelManifestRule {
    override fun check(item: ParcelManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelManifestRequiredDriverIdRule(var message: String) : ParcelManifestRule {
    override fun check(item: ParcelManifest): MutableList<String> {
        if (item.driverId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelManifestRequiredExternalRefRule(var message: String) : ParcelManifestRule {
    override fun check(item: ParcelManifest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelManifestLabelLengthRule(var maxLength: Int) : ParcelManifestRule {
    override fun check(item: ParcelManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel manifest. */
data class ParcelManifestValidator(var rules: MutableList<ParcelManifestRule>) {
    fun validate(item: ParcelManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelManifestValidator(): ParcelManifestValidator {
    return ParcelManifestValidator(
        mutableListOf<ParcelManifestRule>(
            ParcelManifestAllowedLabelRule(mutableListOf<String>("rolling", "strict")),
            ParcelManifestRequiredCodeRule("parcel manifest needs a value"),
            ParcelManifestRequiredDriverIdRule("parcel manifest needs an entry"),
            ParcelManifestRequiredExternalRefRule("parcel manifest needs an entry"),
            ParcelManifestLabelLengthRule(86400),
        ),
    )
}
