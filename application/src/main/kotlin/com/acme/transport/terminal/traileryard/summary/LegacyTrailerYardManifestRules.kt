package com.acme.transport.terminal.traileryard.summary

/** A rule a legacy trailer yard manifest must pass. Check returns its problems, or none. */
interface LegacyTrailerYardManifestRule {
    fun check(item: LegacyTrailerYardManifest): MutableList<String>
}

data class LegacyTrailerYardManifestAmountRangeRule(
    var min: Double,
    var max: Double,
) : LegacyTrailerYardManifestRule {
    override fun check(item: LegacyTrailerYardManifest): MutableList<String> {
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

data class LegacyTrailerYardManifestRequiredCodeRule(var message: String) : LegacyTrailerYardManifestRule {
    override fun check(item: LegacyTrailerYardManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardManifestActiveGuardRule(var limit: Double) : LegacyTrailerYardManifestRule {
    override fun check(item: LegacyTrailerYardManifest): MutableList<String> {
        if (item.active && item.amount > limit) {
            return mutableListOf<String>("amount ${item.amount} exceeds $limit while active")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardManifestCodeLengthRule(var maxLength: Int) : LegacyTrailerYardManifestRule {
    override fun check(item: LegacyTrailerYardManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyTrailerYardManifestRequiredNameRule(var message: String) : LegacyTrailerYardManifestRule {
    override fun check(item: LegacyTrailerYardManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a legacy trailer yard manifest. */
data class LegacyTrailerYardManifestValidator(var rules: MutableList<LegacyTrailerYardManifestRule>) {
    fun validate(item: LegacyTrailerYardManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyTrailerYardManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyTrailerYardManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyTrailerYardManifestValidator(): LegacyTrailerYardManifestValidator {
    return LegacyTrailerYardManifestValidator(
        mutableListOf<LegacyTrailerYardManifestRule>(
            LegacyTrailerYardManifestAmountRangeRule(6.3, 2400.7),
            LegacyTrailerYardManifestRequiredCodeRule("legacy trailer yard manifest needs this field"),
            LegacyTrailerYardManifestActiveGuardRule(9.9),
            LegacyTrailerYardManifestCodeLengthRule(64),
            LegacyTrailerYardManifestRequiredNameRule("legacy trailer yard manifest needs a value"),
        ),
    )
}
