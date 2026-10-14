package com.acme.transport.linehaul.trailer.regionaltrailerrecord

/** A rule a manual trailer target must pass. Check returns its problems, or none. */
interface ManualTrailerTargetRule {
    fun check(item: ManualTrailerTarget): MutableList<String>
}

data class ManualTrailerTargetApprovedGuardRule(var limit: Double) : ManualTrailerTargetRule {
    override fun check(item: ManualTrailerTarget): MutableList<String> {
        if (item.approved && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerTargetRequiredLocaleRule(var message: String) : ManualTrailerTargetRule {
    override fun check(item: ManualTrailerTarget): MutableList<String> {
        if (item.locale.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerTargetNameLengthRule(var maxLength: Int) : ManualTrailerTargetRule {
    override fun check(item: ManualTrailerTarget): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ManualTrailerTargetRequiredNameRule(var message: String) : ManualTrailerTargetRule {
    override fun check(item: ManualTrailerTarget): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a manual trailer target. */
data class ManualTrailerTargetValidator(var rules: MutableList<ManualTrailerTargetRule>) {
    fun validate(item: ManualTrailerTarget): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ManualTrailerTarget) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ManualTrailerTarget>): MutableMap<String, MutableList<String>> {
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

fun defaultManualTrailerTargetValidator(): ManualTrailerTargetValidator {
    return ManualTrailerTargetValidator(
        mutableListOf<ManualTrailerTargetRule>(
            ManualTrailerTargetApprovedGuardRule(9.4),
            ManualTrailerTargetRequiredLocaleRule("manual trailer target needs a value"),
            ManualTrailerTargetNameLengthRule(255),
            ManualTrailerTargetRequiredNameRule("manual trailer target needs a value"),
        ),
    )
}
