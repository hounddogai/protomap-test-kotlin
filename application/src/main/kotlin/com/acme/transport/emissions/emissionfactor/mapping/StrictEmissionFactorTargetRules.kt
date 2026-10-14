package com.acme.transport.emissions.emissionfactor.mapping

import com.acme.transport.kit.text.normalize

/** A rule a strict emission factor target must pass. Check returns its problems, or none. */
interface StrictEmissionFactorTargetRule {
    fun check(item: StrictEmissionFactorTarget): MutableList<String>
}

data class StrictEmissionFactorTargetCodeLengthRule(var maxLength: Int) : StrictEmissionFactorTargetRule {
    override fun check(item: StrictEmissionFactorTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorTargetTrailerIdLengthRule(var maxLength: Int) : StrictEmissionFactorTargetRule {
    override fun check(item: StrictEmissionFactorTarget): MutableList<String> {
        val size = item.trailerId.length
        if (size > maxLength) {
            return mutableListOf<String>("trailer id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorTargetAllowedNameRule(
    var allowed: MutableList<String>,
) : StrictEmissionFactorTargetRule {
    override fun check(item: StrictEmissionFactorTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorTargetCodePrefixRule(var prefix: String) : StrictEmissionFactorTargetRule {
    override fun check(item: StrictEmissionFactorTarget): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class StrictEmissionFactorTargetRequiredSegmentRule(var message: String) : StrictEmissionFactorTargetRule {
    override fun check(item: StrictEmissionFactorTarget): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a strict emission factor target. */
data class StrictEmissionFactorTargetValidator(var rules: MutableList<StrictEmissionFactorTargetRule>) {
    fun validate(item: StrictEmissionFactorTarget): MutableList<String> {
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

    fun requireValid(item: StrictEmissionFactorTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<StrictEmissionFactorTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultStrictEmissionFactorTargetValidator(): StrictEmissionFactorTargetValidator {
    return StrictEmissionFactorTargetValidator(
        mutableListOf<StrictEmissionFactorTargetRule>(
            StrictEmissionFactorTargetCodeLengthRule(3600),
            StrictEmissionFactorTargetTrailerIdLengthRule(16),
            StrictEmissionFactorTargetAllowedNameRule(mutableListOf<String>("lenient", "weighted")),
            StrictEmissionFactorTargetCodePrefixRule("REF"),
            StrictEmissionFactorTargetRequiredSegmentRule("strict emission factor target needs an entry"),
        ),
    )
}
