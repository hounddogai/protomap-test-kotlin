package com.acme.transport.lastmile.parcel.schedule

import com.acme.transport.kit.text.normalize

/** A rule a fallback parcel target must pass. Check returns its problems, or none. */
interface FallbackParcelTargetRule {
    fun check(item: FallbackParcelTarget): MutableList<String>
}

data class FallbackParcelTargetOwnerIdLengthRule(var maxLength: Int) : FallbackParcelTargetRule {
    override fun check(item: FallbackParcelTarget): MutableList<String> {
        val size = item.ownerId.length
        if (size > maxLength) {
            return mutableListOf<String>("owner id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackParcelTargetCodeLengthRule(var maxLength: Int) : FallbackParcelTargetRule {
    override fun check(item: FallbackParcelTarget): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackParcelTargetRequiredCodeRule(var message: String) : FallbackParcelTargetRule {
    override fun check(item: FallbackParcelTarget): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackParcelTargetRequiredSummaryRule(var message: String) : FallbackParcelTargetRule {
    override fun check(item: FallbackParcelTarget): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackParcelTargetAllowedSummaryRule(var allowed: MutableList<String>) : FallbackParcelTargetRule {
    override fun check(item: FallbackParcelTarget): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.summary))) {
            return mutableListOf<String>("summary ${item.summary} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class FallbackParcelTargetSummaryLengthRule(var maxLength: Int) : FallbackParcelTargetRule {
    override fun check(item: FallbackParcelTarget): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback parcel target. */
data class FallbackParcelTargetValidator(var rules: MutableList<FallbackParcelTargetRule>) {
    fun validate(item: FallbackParcelTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackParcelTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackParcelTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackParcelTargetValidator(): FallbackParcelTargetValidator {
    return FallbackParcelTargetValidator(
        mutableListOf<FallbackParcelTargetRule>(
            FallbackParcelTargetOwnerIdLengthRule(16),
            FallbackParcelTargetCodeLengthRule(64),
            FallbackParcelTargetRequiredCodeRule("fallback parcel target needs this field"),
            FallbackParcelTargetRequiredSummaryRule("fallback parcel target needs a value"),
            FallbackParcelTargetAllowedSummaryRule(mutableListOf<String>("global")),
            FallbackParcelTargetSummaryLengthRule(255),
        ),
    )
}
