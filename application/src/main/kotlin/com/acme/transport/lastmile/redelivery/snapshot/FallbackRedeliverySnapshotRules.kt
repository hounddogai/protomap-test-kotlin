package com.acme.transport.lastmile.redelivery.snapshot

/** A rule a fallback redelivery snapshot must pass. Check returns its problems, or none. */
interface FallbackRedeliverySnapshotRule {
    fun check(item: FallbackRedeliverySnapshot): MutableList<String>
}

data class FallbackRedeliverySnapshotNameLengthRule(var maxLength: Int) : FallbackRedeliverySnapshotRule {
    override fun check(item: FallbackRedeliverySnapshot): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRedeliverySnapshotExternalRefLengthRule(
    var maxLength: Int,
) : FallbackRedeliverySnapshotRule {
    override fun check(item: FallbackRedeliverySnapshot): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRedeliverySnapshotCodePrefixRule(var prefix: String) : FallbackRedeliverySnapshotRule {
    override fun check(item: FallbackRedeliverySnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRedeliverySnapshotRequiredCodeRule(var message: String) : FallbackRedeliverySnapshotRule {
    override fun check(item: FallbackRedeliverySnapshot): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRedeliverySnapshotSequenceRangeRule(
    var min: Int,
    var max: Int,
) : FallbackRedeliverySnapshotRule {
    override fun check(item: FallbackRedeliverySnapshot): MutableList<String> {
        val value = item.sequence
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("sequence $value is below $min")
        }
        if (value > max) {
            problems.add("sequence $value is above $max")
        }
        return problems
    }
}

data class FallbackRedeliverySnapshotRequiredNameRule(var message: String) : FallbackRedeliverySnapshotRule {
    override fun check(item: FallbackRedeliverySnapshot): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback redelivery snapshot. */
data class FallbackRedeliverySnapshotValidator(var rules: MutableList<FallbackRedeliverySnapshotRule>) {
    fun validate(item: FallbackRedeliverySnapshot): MutableList<String> {
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

    fun requireValid(item: FallbackRedeliverySnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRedeliverySnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRedeliverySnapshotValidator(): FallbackRedeliverySnapshotValidator {
    return FallbackRedeliverySnapshotValidator(
        mutableListOf<FallbackRedeliverySnapshotRule>(
            FallbackRedeliverySnapshotNameLengthRule(1000),
            FallbackRedeliverySnapshotExternalRefLengthRule(255),
            FallbackRedeliverySnapshotCodePrefixRule("Q"),
            FallbackRedeliverySnapshotRequiredCodeRule("fallback redelivery snapshot needs an entry"),
            FallbackRedeliverySnapshotSequenceRangeRule(16, 32),
            FallbackRedeliverySnapshotRequiredNameRule("fallback redelivery snapshot needs a value"),
        ),
    )
}
