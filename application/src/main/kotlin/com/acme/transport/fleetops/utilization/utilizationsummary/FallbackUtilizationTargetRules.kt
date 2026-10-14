package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.kit.text.normalize

/** A rule a fallback utilization target must pass. Check returns its problems, or none. */
interface FallbackUtilizationTargetRule {
    fun check(item: FallbackUtilizationTarget): MutableList<String>
}

data class FallbackUtilizationTargetRequiredTimezoneRule(var message: String) : FallbackUtilizationTargetRule {
    override fun check(item: FallbackUtilizationTarget): MutableList<String> {
        if (item.timezone.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackUtilizationTargetAllowedTimezoneRule(
    var allowed: MutableList<String>,
) : FallbackUtilizationTargetRule {
    override fun check(item: FallbackUtilizationTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.timezone))) {
            return mutableListOf<String>("timezone ${item.timezone} is expired")
        }
        return mutableListOf<String>()
    }
}

data class FallbackUtilizationTargetLabelLengthRule(var maxLength: Int) : FallbackUtilizationTargetRule {
    override fun check(item: FallbackUtilizationTarget): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackUtilizationTargetRequiredNotesRule(var message: String) : FallbackUtilizationTargetRule {
    override fun check(item: FallbackUtilizationTarget): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback utilization target. */
data class FallbackUtilizationTargetValidator(var rules: MutableList<FallbackUtilizationTargetRule>) {
    fun validate(item: FallbackUtilizationTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackUtilizationTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackUtilizationTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackUtilizationTargetValidator(): FallbackUtilizationTargetValidator {
    return FallbackUtilizationTargetValidator(
        mutableListOf<FallbackUtilizationTargetRule>(
            FallbackUtilizationTargetRequiredTimezoneRule("fallback utilization target needs this field"),
            FallbackUtilizationTargetAllowedTimezoneRule(
                mutableListOf<String>("manual", "default", "scheduled"),
            ),
            FallbackUtilizationTargetLabelLengthRule(255),
            FallbackUtilizationTargetRequiredNotesRule("fallback utilization target needs an entry"),
        ),
    )
}
