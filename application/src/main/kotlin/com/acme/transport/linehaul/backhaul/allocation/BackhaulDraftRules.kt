package com.acme.transport.linehaul.backhaul.allocation

/** A rule a backhaul draft must pass. Check returns its problems, or none. */
interface BackhaulDraftRule {
    fun check(item: BackhaulDraft): MutableList<String>
}

data class BackhaulDraftRequiredTrailerIdRule(var message: String) : BackhaulDraftRule {
    override fun check(item: BackhaulDraft): MutableList<String> {
        if (item.trailerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulDraftRequiredLabelRule(var message: String) : BackhaulDraftRule {
    override fun check(item: BackhaulDraft): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulDraftRequiredCodeRule(var message: String) : BackhaulDraftRule {
    override fun check(item: BackhaulDraft): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulDraftCodePrefixRule(var prefix: String) : BackhaulDraftRule {
    override fun check(item: BackhaulDraft): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulDraftLoadKgRangeRule(var min: Double, var max: Double) : BackhaulDraftRule {
    override fun check(item: BackhaulDraft): MutableList<String> {
        val value = item.loadKg
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("load kg $value is below $min")
        }
        if (value > max) {
            problems.add("load kg $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a backhaul draft. */
data class BackhaulDraftValidator(var rules: MutableList<BackhaulDraftRule>) {
    fun validate(item: BackhaulDraft): MutableList<String> {
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

    fun requireValid(item: BackhaulDraft) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulDraft>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulDraftValidator(): BackhaulDraftValidator {
    return BackhaulDraftValidator(
        mutableListOf<BackhaulDraftRule>(
            BackhaulDraftRequiredTrailerIdRule("backhaul draft needs a value"),
            BackhaulDraftRequiredLabelRule("backhaul draft needs this field"),
            BackhaulDraftRequiredCodeRule("backhaul draft needs a value"),
            BackhaulDraftCodePrefixRule("REF"),
            BackhaulDraftLoadKgRangeRule(8.6, 4265.2),
        ),
    )
}
