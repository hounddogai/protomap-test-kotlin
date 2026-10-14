package com.acme.transport.safety.violation.manifest

/** A rule a violation manifest must pass. Check returns its problems, or none. */
interface ViolationManifestRule {
    fun check(item: ViolationManifest): MutableList<String>
}

data class ViolationManifestNameLengthRule(var maxLength: Int) : ViolationManifestRule {
    override fun check(item: ViolationManifest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ViolationManifestCodePrefixRule(var prefix: String) : ViolationManifestRule {
    override fun check(item: ViolationManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ViolationManifestRequiredCodeRule(var message: String) : ViolationManifestRule {
    override fun check(item: ViolationManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a violation manifest. */
data class ViolationManifestValidator(var rules: MutableList<ViolationManifestRule>) {
    fun validate(item: ViolationManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ViolationManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ViolationManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultViolationManifestValidator(): ViolationManifestValidator {
    return ViolationManifestValidator(
        mutableListOf<ViolationManifestRule>(
            ViolationManifestNameLengthRule(64),
            ViolationManifestCodePrefixRule("REF"),
            ViolationManifestRequiredCodeRule("violation manifest needs an entry"),
        ),
    )
}
