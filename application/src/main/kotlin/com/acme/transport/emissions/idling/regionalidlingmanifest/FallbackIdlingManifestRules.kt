package com.acme.transport.emissions.idling.regionalidlingmanifest

/** A rule a fallback idling manifest must pass. Check returns its problems, or none. */
interface FallbackIdlingManifestRule {
    fun check(item: FallbackIdlingManifest): MutableList<String>
}

data class FallbackIdlingManifestRequiredContactEmailRule(var message: String) : FallbackIdlingManifestRule {
    override fun check(item: FallbackIdlingManifest): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingManifestNameLengthRule(var maxLength: Int) : FallbackIdlingManifestRule {
    override fun check(item: FallbackIdlingManifest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingManifestRequiredCodeRule(var message: String) : FallbackIdlingManifestRule {
    override fun check(item: FallbackIdlingManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingManifestCodeLengthRule(var maxLength: Int) : FallbackIdlingManifestRule {
    override fun check(item: FallbackIdlingManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackIdlingManifestPriorityRangeRule(var min: Int, var max: Int) : FallbackIdlingManifestRule {
    override fun check(item: FallbackIdlingManifest): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback idling manifest. */
data class FallbackIdlingManifestValidator(var rules: MutableList<FallbackIdlingManifestRule>) {
    fun validate(item: FallbackIdlingManifest): MutableList<String> {
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

    fun requireValid(item: FallbackIdlingManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackIdlingManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackIdlingManifestValidator(): FallbackIdlingManifestValidator {
    return FallbackIdlingManifestValidator(
        mutableListOf<FallbackIdlingManifestRule>(
            FallbackIdlingManifestRequiredContactEmailRule("fallback idling manifest needs this field"),
            FallbackIdlingManifestNameLengthRule(120),
            FallbackIdlingManifestRequiredCodeRule("fallback idling manifest needs this field"),
            FallbackIdlingManifestCodeLengthRule(1000),
            FallbackIdlingManifestPriorityRangeRule(16, 1000),
        ),
    )
}
