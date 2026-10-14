package com.acme.transport.crewing.restbreak.milestone

/** A rule a fallback rest break override must pass. Check returns its problems, or none. */
interface FallbackRestBreakOverrideRule {
    fun check(item: FallbackRestBreakOverride): MutableList<String>
}

data class FallbackRestBreakOverrideRequiredDepotRule(var message: String) : FallbackRestBreakOverrideRule {
    override fun check(item: FallbackRestBreakOverride): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackRestBreakOverrideCodeLengthRule(var maxLength: Int) : FallbackRestBreakOverrideRule {
    override fun check(item: FallbackRestBreakOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRestBreakOverrideContactEmailLengthRule(var maxLength: Int) : FallbackRestBreakOverrideRule {
    override fun check(item: FallbackRestBreakOverride): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackRestBreakOverrideArchivedGuardRule(var limit: Double) : FallbackRestBreakOverrideRule {
    override fun check(item: FallbackRestBreakOverride): MutableList<String> {
        if (item.archived && item.distanceKm > limit) {
            return mutableListOf<String>("distance km ${item.distanceKm} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback rest break override. */
data class FallbackRestBreakOverrideValidator(var rules: MutableList<FallbackRestBreakOverrideRule>) {
    fun validate(item: FallbackRestBreakOverride): MutableList<String> {
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

    fun requireValid(item: FallbackRestBreakOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackRestBreakOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackRestBreakOverrideValidator(): FallbackRestBreakOverrideValidator {
    return FallbackRestBreakOverrideValidator(
        mutableListOf<FallbackRestBreakOverrideRule>(
            FallbackRestBreakOverrideRequiredDepotRule("fallback rest break override needs a value"),
            FallbackRestBreakOverrideCodeLengthRule(86400),
            FallbackRestBreakOverrideContactEmailLengthRule(120),
            FallbackRestBreakOverrideArchivedGuardRule(9.3),
        ),
    )
}
