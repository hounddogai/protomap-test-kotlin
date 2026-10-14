package com.acme.transport.terminal.dwell.dwellcycle

/** A rule a regional dwell digest must pass. Check returns its problems, or none. */
interface RegionalDwellDigestRule {
    fun check(item: RegionalDwellDigest): MutableList<String>
}

data class RegionalDwellDigestAmountRangeRule(var min: Double, var max: Double) : RegionalDwellDigestRule {
    override fun check(item: RegionalDwellDigest): MutableList<String> {
        val value = item.amount
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("amount $value is below $min")
        }
        if (value > max) {
            problems.add("amount $value is above $max")
        }
        return problems
    }
}

data class RegionalDwellDigestRequiredLabelRule(var message: String) : RegionalDwellDigestRule {
    override fun check(item: RegionalDwellDigest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RegionalDwellDigestCodeLengthRule(var maxLength: Int) : RegionalDwellDigestRule {
    override fun check(item: RegionalDwellDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RegionalDwellDigestTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : RegionalDwellDigestRule {
    override fun check(item: RegionalDwellDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class RegionalDwellDigestCodePrefixRule(var prefix: String) : RegionalDwellDigestRule {
    override fun check(item: RegionalDwellDigest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a regional dwell digest. */
data class RegionalDwellDigestValidator(var rules: MutableList<RegionalDwellDigestRule>) {
    fun validate(item: RegionalDwellDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RegionalDwellDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RegionalDwellDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultRegionalDwellDigestValidator(): RegionalDwellDigestValidator {
    return RegionalDwellDigestValidator(
        mutableListOf<RegionalDwellDigestRule>(
            RegionalDwellDigestAmountRangeRule(7.3, 4778.7),
            RegionalDwellDigestRequiredLabelRule("regional dwell digest needs an entry"),
            RegionalDwellDigestCodeLengthRule(1000),
            RegionalDwellDigestTagsLimitRule(1000, mutableListOf<String>("batched", "seasonal", "weighted")),
            RegionalDwellDigestCodePrefixRule("Q"),
        ),
    )
}
