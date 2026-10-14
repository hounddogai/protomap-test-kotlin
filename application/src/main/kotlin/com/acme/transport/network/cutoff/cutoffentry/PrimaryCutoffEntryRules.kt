package com.acme.transport.network.cutoff.cutoffentry

/** A rule a primary cutoff entry must pass. Check returns its problems, or none. */
interface PrimaryCutoffEntryRule {
    fun check(item: PrimaryCutoffEntry): MutableList<String>
}

data class PrimaryCutoffEntryRequiredRouteCodeRule(var message: String) : PrimaryCutoffEntryRule {
    override fun check(item: PrimaryCutoffEntry): MutableList<String> {
        if (item.routeCode.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCutoffEntryApprovedGuardRule(var limit: Double) : PrimaryCutoffEntryRule {
    override fun check(item: PrimaryCutoffEntry): MutableList<String> {
        if (item.approved && item.rate > limit) {
            return mutableListOf<String>("rate ${item.rate} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCutoffEntryRequiredChannelRule(var message: String) : PrimaryCutoffEntryRule {
    override fun check(item: PrimaryCutoffEntry): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryCutoffEntryTitleLengthRule(var maxLength: Int) : PrimaryCutoffEntryRule {
    override fun check(item: PrimaryCutoffEntry): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary cutoff entry. */
data class PrimaryCutoffEntryValidator(var rules: MutableList<PrimaryCutoffEntryRule>) {
    fun validate(item: PrimaryCutoffEntry): MutableList<String> {
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

    fun requireValid(item: PrimaryCutoffEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryCutoffEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryCutoffEntryValidator(): PrimaryCutoffEntryValidator {
    return PrimaryCutoffEntryValidator(
        mutableListOf<PrimaryCutoffEntryRule>(
            PrimaryCutoffEntryRequiredRouteCodeRule("primary cutoff entry needs an entry"),
            PrimaryCutoffEntryApprovedGuardRule(6.8),
            PrimaryCutoffEntryRequiredChannelRule("primary cutoff entry needs an entry"),
            PrimaryCutoffEntryTitleLengthRule(120),
        ),
    )
}
