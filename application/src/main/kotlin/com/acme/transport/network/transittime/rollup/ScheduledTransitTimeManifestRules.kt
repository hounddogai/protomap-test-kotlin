package com.acme.transport.network.transittime.rollup

/** A rule a scheduled transit time manifest must pass. Check returns its problems, or none. */
interface ScheduledTransitTimeManifestRule {
    fun check(item: ScheduledTransitTimeManifest): MutableList<String>
}

data class ScheduledTransitTimeManifestRequiredCodeRule(
    var message: String,
) : ScheduledTransitTimeManifestRule {
    override fun check(item: ScheduledTransitTimeManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTransitTimeManifestScoreRangeRule(
    var min: Double,
    var max: Double,
) : ScheduledTransitTimeManifestRule {
    override fun check(item: ScheduledTransitTimeManifest): MutableList<String> {
        val value = item.score
        if (value < min || value > max) {
            return mutableListOf<String>("score $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTransitTimeManifestCodeLengthRule(var maxLength: Int) : ScheduledTransitTimeManifestRule {
    override fun check(item: ScheduledTransitTimeManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTransitTimeManifestRequiredNameRule(
    var message: String,
) : ScheduledTransitTimeManifestRule {
    override fun check(item: ScheduledTransitTimeManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ScheduledTransitTimeManifestCodePrefixRule(var prefix: String) : ScheduledTransitTimeManifestRule {
    override fun check(item: ScheduledTransitTimeManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a scheduled transit time manifest. */
data class ScheduledTransitTimeManifestValidator(var rules: MutableList<ScheduledTransitTimeManifestRule>) {
    fun validate(item: ScheduledTransitTimeManifest): MutableList<String> {
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

    fun requireValid(item: ScheduledTransitTimeManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ScheduledTransitTimeManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultScheduledTransitTimeManifestValidator(): ScheduledTransitTimeManifestValidator {
    return ScheduledTransitTimeManifestValidator(
        mutableListOf<ScheduledTransitTimeManifestRule>(
            ScheduledTransitTimeManifestRequiredCodeRule("scheduled transit time manifest needs a value"),
            ScheduledTransitTimeManifestScoreRangeRule(9.7, 1786.7),
            ScheduledTransitTimeManifestCodeLengthRule(80),
            ScheduledTransitTimeManifestRequiredNameRule("scheduled transit time manifest needs this field"),
            ScheduledTransitTimeManifestCodePrefixRule("REF"),
        ),
    )
}
