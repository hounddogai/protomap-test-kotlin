package com.acme.transport.network.transittime.draft

/** A rule a lenient transit time snapshot must pass. Check returns its problems, or none. */
interface LenientTransitTimeSnapshotRule {
    fun check(item: LenientTransitTimeSnapshot): MutableList<String>
}

data class LenientTransitTimeSnapshotRequiredLocaleRule(var message: String) : LenientTransitTimeSnapshotRule {
    override fun check(item: LenientTransitTimeSnapshot): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientTransitTimeSnapshotCodePrefixRule(var prefix: String) : LenientTransitTimeSnapshotRule {
    override fun check(item: LenientTransitTimeSnapshot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class LenientTransitTimeSnapshotCodeLengthRule(var maxLength: Int) : LenientTransitTimeSnapshotRule {
    override fun check(item: LenientTransitTimeSnapshot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient transit time snapshot. */
data class LenientTransitTimeSnapshotValidator(var rules: MutableList<LenientTransitTimeSnapshotRule>) {
    fun validate(item: LenientTransitTimeSnapshot): MutableList<String> {
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

    fun requireValid(item: LenientTransitTimeSnapshot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientTransitTimeSnapshot>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientTransitTimeSnapshotValidator(): LenientTransitTimeSnapshotValidator {
    return LenientTransitTimeSnapshotValidator(
        mutableListOf<LenientTransitTimeSnapshotRule>(
            LenientTransitTimeSnapshotRequiredLocaleRule("lenient transit time snapshot needs an entry"),
            LenientTransitTimeSnapshotCodePrefixRule("Q"),
            LenientTransitTimeSnapshotCodeLengthRule(255),
        ),
    )
}
