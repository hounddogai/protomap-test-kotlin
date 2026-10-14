package com.acme.transport.linehaul.laneplan.review

/** A rule a pending lane plan item must pass. Check returns its problems, or none. */
interface PendingLanePlanItemRule {
    fun check(item: PendingLanePlanItem): MutableList<String>
}

data class PendingLanePlanItemSummaryLengthRule(var maxLength: Int) : PendingLanePlanItemRule {
    override fun check(item: PendingLanePlanItem): MutableList<String> {
        val size = item.summary.length
        if (size > maxLength) {
            return mutableListOf<String>("summary is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanItemNameLengthRule(var maxLength: Int) : PendingLanePlanItemRule {
    override fun check(item: PendingLanePlanItem): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanItemCodePrefixRule(var prefix: String) : PendingLanePlanItemRule {
    override fun check(item: PendingLanePlanItem): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanItemEndsAtRangeRule(var min: Int, var max: Int) : PendingLanePlanItemRule {
    override fun check(item: PendingLanePlanItem): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PendingLanePlanItemCodeLengthRule(var maxLength: Int) : PendingLanePlanItemRule {
    override fun check(item: PendingLanePlanItem): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a pending lane plan item. */
data class PendingLanePlanItemValidator(var rules: MutableList<PendingLanePlanItemRule>) {
    fun validate(item: PendingLanePlanItem): MutableList<String> {
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

    fun requireValid(item: PendingLanePlanItem) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PendingLanePlanItem>): MutableMap<String, MutableList<String>> {
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

fun defaultPendingLanePlanItemValidator(): PendingLanePlanItemValidator {
    return PendingLanePlanItemValidator(
        mutableListOf<PendingLanePlanItemRule>(
            PendingLanePlanItemSummaryLengthRule(255),
            PendingLanePlanItemNameLengthRule(86400),
            PendingLanePlanItemCodePrefixRule("X"),
            PendingLanePlanItemEndsAtRangeRule(32, 3600),
            PendingLanePlanItemCodeLengthRule(32),
        ),
    )
}
