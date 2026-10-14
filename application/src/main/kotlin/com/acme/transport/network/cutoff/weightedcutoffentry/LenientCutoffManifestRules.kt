package com.acme.transport.network.cutoff.weightedcutoffentry

/** A rule a lenient cutoff manifest must pass. Check returns its problems, or none. */
interface LenientCutoffManifestRule {
    fun check(item: LenientCutoffManifest): MutableList<String>
}

data class LenientCutoffManifestNameLengthRule(var maxLength: Int) : LenientCutoffManifestRule {
    override fun check(item: LenientCutoffManifest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LenientCutoffManifestCeilingRangeRule(var min: Double, var max: Double) : LenientCutoffManifestRule {
    override fun check(item: LenientCutoffManifest): MutableList<String> {
        val value = item.ceiling
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("ceiling $value is below $min")
        }
        if (value > max) {
            problems.add("ceiling $value is above $max")
        }
        return problems
    }
}

data class LenientCutoffManifestRequiredSourceRule(var message: String) : LenientCutoffManifestRule {
    override fun check(item: LenientCutoffManifest): MutableList<String> {
        if (item.source.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LenientCutoffManifestAmountRangeRule(var min: Double, var max: Double) : LenientCutoffManifestRule {
    override fun check(item: LenientCutoffManifest): MutableList<String> {
        val value = item.amount
        if (value < min || value > max) {
            return mutableListOf<String>("amount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a lenient cutoff manifest. */
data class LenientCutoffManifestValidator(var rules: MutableList<LenientCutoffManifestRule>) {
    fun validate(item: LenientCutoffManifest): MutableList<String> {
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

    fun requireValid(item: LenientCutoffManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LenientCutoffManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLenientCutoffManifestValidator(): LenientCutoffManifestValidator {
    return LenientCutoffManifestValidator(
        mutableListOf<LenientCutoffManifestRule>(
            LenientCutoffManifestNameLengthRule(64),
            LenientCutoffManifestCeilingRangeRule(2.7, 3770.1),
            LenientCutoffManifestRequiredSourceRule("lenient cutoff manifest needs an entry"),
            LenientCutoffManifestAmountRangeRule(9.6, 536.5),
        ),
    )
}
