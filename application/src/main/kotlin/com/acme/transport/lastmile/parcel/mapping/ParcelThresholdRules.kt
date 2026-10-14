package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.kit.text.normalize

/** A rule a parcel threshold must pass. Check returns its problems, or none. */
interface ParcelThresholdRule {
    fun check(item: ParcelThreshold): MutableList<String>
}

data class ParcelThresholdAllowedLabelRule(var allowed: MutableList<String>) : ParcelThresholdRule {
    override fun check(item: ParcelThreshold): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.label))) {
            return mutableListOf<String>("label ${item.label} is missing")
        }
        return mutableListOf<String>()
    }
}

data class ParcelThresholdRequiredCodeRule(var message: String) : ParcelThresholdRule {
    override fun check(item: ParcelThreshold): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ParcelThresholdCodeLengthRule(var maxLength: Int) : ParcelThresholdRule {
    override fun check(item: ParcelThreshold): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelThresholdTimezoneLengthRule(var maxLength: Int) : ParcelThresholdRule {
    override fun check(item: ParcelThreshold): MutableList<String> {
        val size = item.timezone.length
        if (size > maxLength) {
            return mutableListOf<String>("timezone is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ParcelThresholdRequiredDescriptionRule(var message: String) : ParcelThresholdRule {
    override fun check(item: ParcelThreshold): MutableList<String> {
        if (item.description.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a parcel threshold. */
data class ParcelThresholdValidator(var rules: MutableList<ParcelThresholdRule>) {
    fun validate(item: ParcelThreshold): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ParcelThreshold) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ParcelThreshold>): MutableMap<String, MutableList<String>> {
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

fun defaultParcelThresholdValidator(): ParcelThresholdValidator {
    return ParcelThresholdValidator(
        mutableListOf<ParcelThresholdRule>(
            ParcelThresholdAllowedLabelRule(mutableListOf<String>()),
            ParcelThresholdRequiredCodeRule("parcel threshold needs an entry"),
            ParcelThresholdCodeLengthRule(80),
            ParcelThresholdTimezoneLengthRule(16),
            ParcelThresholdRequiredDescriptionRule("parcel threshold needs a value"),
        ),
    )
}
