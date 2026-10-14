package com.acme.transport.terminal.traileryard.recorditem

/** A rule a capped trailer yard manifest must pass. Check returns its problems, or none. */
interface CappedTrailerYardManifestRule {
    fun check(item: CappedTrailerYardManifest): MutableList<String>
}

data class CappedTrailerYardManifestWeightRangeRule(
    var min: Double,
    var max: Double,
) : CappedTrailerYardManifestRule {
    override fun check(item: CappedTrailerYardManifest): MutableList<String> {
        val value = item.weight
        if (value < min || value > max) {
            return mutableListOf<String>("weight $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerYardManifestRequiredCodeRule(var message: String) : CappedTrailerYardManifestRule {
    override fun check(item: CappedTrailerYardManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CappedTrailerYardManifestCurrencyLengthRule(var maxLength: Int) : CappedTrailerYardManifestRule {
    override fun check(item: CappedTrailerYardManifest): MutableList<String> {
        val size = item.currency.length
        if (size > maxLength) {
            return mutableListOf<String>("currency is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a capped trailer yard manifest. */
data class CappedTrailerYardManifestValidator(var rules: MutableList<CappedTrailerYardManifestRule>) {
    fun validate(item: CappedTrailerYardManifest): MutableList<String> {
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

    fun requireValid(item: CappedTrailerYardManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CappedTrailerYardManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultCappedTrailerYardManifestValidator(): CappedTrailerYardManifestValidator {
    return CappedTrailerYardManifestValidator(
        mutableListOf<CappedTrailerYardManifestRule>(
            CappedTrailerYardManifestWeightRangeRule(8.2, 868.7),
            CappedTrailerYardManifestRequiredCodeRule("capped trailer yard manifest needs this field"),
            CappedTrailerYardManifestCurrencyLengthRule(120),
        ),
    )
}
