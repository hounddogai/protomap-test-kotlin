package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.text.normalize

/** A rule a lenient route offset checkpoint must pass. Check returns its problems, or none. */
interface LenientRouteOffsetCheckpointRule {
    fun check(item: LenientRouteOffsetCheckpoint): MutableList<String>
}

data class LenientRouteOffsetCheckpointContactEmailLengthRule(
    var maxLength: Int,
) : LenientRouteOffsetCheckpointRule {
    override fun check(item: LenientRouteOffsetCheckpoint): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientRouteOffsetCheckpointRequiredTitleRule(
    var message: String,
) : LenientRouteOffsetCheckpointRule {
    override fun check(item: LenientRouteOffsetCheckpoint): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientRouteOffsetCheckpointAllowedCodeRule(
    var allowed: MutableList<String>,
) : LenientRouteOffsetCheckpointRule {
    override fun check(item: LenientRouteOffsetCheckpoint): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

data class LenientRouteOffsetCheckpointRequiredContactEmailRule(
    var message: String,
) : LenientRouteOffsetCheckpointRule {
    override fun check(item: LenientRouteOffsetCheckpoint): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient route offset checkpoint. */
data class LenientRouteOffsetCheckpointValidator(var rules: MutableList<LenientRouteOffsetCheckpointRule>) {
    fun validate(item: LenientRouteOffsetCheckpoint): MutableList<String> {
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

    fun requireValid(item: LenientRouteOffsetCheckpoint) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientRouteOffsetCheckpoint>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientRouteOffsetCheckpointValidator(): LenientRouteOffsetCheckpointValidator {
    return LenientRouteOffsetCheckpointValidator(
        mutableListOf<LenientRouteOffsetCheckpointRule>(
            LenientRouteOffsetCheckpointContactEmailLengthRule(3600),
            LenientRouteOffsetCheckpointRequiredTitleRule("lenient route offset checkpoint needs this field"),
            LenientRouteOffsetCheckpointAllowedCodeRule(mutableListOf<String>()),
            LenientRouteOffsetCheckpointRequiredContactEmailRule(
                "lenient route offset checkpoint needs a value",
            ),
        ),
    )
}
