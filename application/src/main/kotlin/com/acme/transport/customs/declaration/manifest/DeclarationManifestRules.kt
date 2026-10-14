package com.acme.transport.customs.declaration.manifest

/** A rule a declaration manifest must pass. Check returns its problems, or none. */
interface DeclarationManifestRule {
    fun check(item: DeclarationManifest): MutableList<String>
}

data class DeclarationManifestRequiredTitleRule(var message: String) : DeclarationManifestRule {
    override fun check(item: DeclarationManifest): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationManifestCodePrefixRule(var prefix: String) : DeclarationManifestRule {
    override fun check(item: DeclarationManifest): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class DeclarationManifestRequiredExternalRefRule(var message: String) : DeclarationManifestRule {
    override fun check(item: DeclarationManifest): MutableList<String> {
        if (item.externalRef.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DeclarationManifestLimitRangeRule(var min: Int, var max: Int) : DeclarationManifestRule {
    override fun check(item: DeclarationManifest): MutableList<String> {
        val value = item.limit
        if (value < min || value > max) {
            return mutableListOf<String>("limit $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a declaration manifest. */
data class DeclarationManifestValidator(var rules: MutableList<DeclarationManifestRule>) {
    fun validate(item: DeclarationManifest): MutableList<String> {
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

    fun requireValid(item: DeclarationManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DeclarationManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultDeclarationManifestValidator(): DeclarationManifestValidator {
    return DeclarationManifestValidator(
        mutableListOf<DeclarationManifestRule>(
            DeclarationManifestRequiredTitleRule("declaration manifest needs an entry"),
            DeclarationManifestCodePrefixRule("Q"),
            DeclarationManifestRequiredExternalRefRule("declaration manifest needs an entry"),
            DeclarationManifestLimitRangeRule(64, 3600),
        ),
    )
}
