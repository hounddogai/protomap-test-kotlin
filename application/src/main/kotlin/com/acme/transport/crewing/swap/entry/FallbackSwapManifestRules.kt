package com.acme.transport.crewing.swap.entry

/** A rule a fallback swap manifest must pass. Check returns its problems, or none. */
interface FallbackSwapManifestRule {
    fun check(item: FallbackSwapManifest): MutableList<String>
}

data class FallbackSwapManifestRequiredCodeRule(var message: String) : FallbackSwapManifestRule {
    override fun check(item: FallbackSwapManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackSwapManifestUpdatedAtRangeRule(var min: Int, var max: Int) : FallbackSwapManifestRule {
    override fun check(item: FallbackSwapManifest): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackSwapManifestCodeLengthRule(var maxLength: Int) : FallbackSwapManifestRule {
    override fun check(item: FallbackSwapManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback swap manifest. */
data class FallbackSwapManifestValidator(var rules: MutableList<FallbackSwapManifestRule>) {
    fun validate(item: FallbackSwapManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: FallbackSwapManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackSwapManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackSwapManifestValidator(): FallbackSwapManifestValidator {
    return FallbackSwapManifestValidator(
        mutableListOf<FallbackSwapManifestRule>(
            FallbackSwapManifestRequiredCodeRule("fallback swap manifest needs this field"),
            FallbackSwapManifestUpdatedAtRangeRule(80, 86400),
            FallbackSwapManifestCodeLengthRule(16),
        ),
    )
}
