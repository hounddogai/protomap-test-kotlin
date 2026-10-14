package com.acme.transport.crewing.restbreak.breakvariance

/** A rule a tiered rest break manifest must pass. Check returns its problems, or none. */
interface TieredRestBreakManifestRule {
    fun check(item: TieredRestBreakManifest): MutableList<String>
}

data class TieredRestBreakManifestCodeLengthRule(var maxLength: Int) : TieredRestBreakManifestRule {
    override fun check(item: TieredRestBreakManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredRestBreakManifestRequiredCodeRule(var message: String) : TieredRestBreakManifestRule {
    override fun check(item: TieredRestBreakManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRestBreakManifestOdometerRangeRule(var min: Int, var max: Int) : TieredRestBreakManifestRule {
    override fun check(item: TieredRestBreakManifest): MutableList<String> {
        val value = item.odometer
        if (value < min || value > max) {
            return mutableListOf<String>("odometer $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredRestBreakManifestThresholdRangeRule(
    var min: Double,
    var max: Double,
) : TieredRestBreakManifestRule {
    override fun check(item: TieredRestBreakManifest): MutableList<String> {
        val value = item.threshold
        if (value < min || value > max) {
            return mutableListOf<String>("threshold $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class TieredRestBreakManifestRequiredNameRule(var message: String) : TieredRestBreakManifestRule {
    override fun check(item: TieredRestBreakManifest): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredRestBreakManifestCodePrefixRule(var prefix: String) : TieredRestBreakManifestRule {
    override fun check(item: TieredRestBreakManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered rest break manifest. */
data class TieredRestBreakManifestValidator(var rules: MutableList<TieredRestBreakManifestRule>) {
    fun validate(item: TieredRestBreakManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredRestBreakManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredRestBreakManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredRestBreakManifestValidator(): TieredRestBreakManifestValidator {
    return TieredRestBreakManifestValidator(
        mutableListOf<TieredRestBreakManifestRule>(
            TieredRestBreakManifestCodeLengthRule(86400),
            TieredRestBreakManifestRequiredCodeRule("tiered rest break manifest needs a value"),
            TieredRestBreakManifestOdometerRangeRule(1000, 86400),
            TieredRestBreakManifestThresholdRangeRule(9.8, 2826.1),
            TieredRestBreakManifestRequiredNameRule("tiered rest break manifest needs a value"),
            TieredRestBreakManifestCodePrefixRule("X"),
        ),
    )
}
