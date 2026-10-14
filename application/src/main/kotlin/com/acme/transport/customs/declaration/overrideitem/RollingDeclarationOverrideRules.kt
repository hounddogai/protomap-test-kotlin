package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.text.normalize

/** A rule a rolling declaration override must pass. Check returns its problems, or none. */
interface RollingDeclarationOverrideRule {
    fun check(item: RollingDeclarationOverride): MutableList<String>
}

data class RollingDeclarationOverrideCodePrefixRule(var prefix: String) : RollingDeclarationOverrideRule {
    override fun check(item: RollingDeclarationOverride): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationOverrideCodeLengthRule(var maxLength: Int) : RollingDeclarationOverrideRule {
    override fun check(item: RollingDeclarationOverride): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationOverrideAllowedTitleRule(
    var allowed: MutableList<String>,
) : RollingDeclarationOverrideRule {
    override fun check(item: RollingDeclarationOverride): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is blocked")
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationOverrideRequiredTitleRule(var message: String) : RollingDeclarationOverrideRule {
    override fun check(item: RollingDeclarationOverride): MutableList<String> {
        if (item.title.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling declaration override. */
data class RollingDeclarationOverrideValidator(var rules: MutableList<RollingDeclarationOverrideRule>) {
    fun validate(item: RollingDeclarationOverride): MutableList<String> {
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

    fun requireValid(item: RollingDeclarationOverride) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDeclarationOverride>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDeclarationOverrideValidator(): RollingDeclarationOverrideValidator {
    return RollingDeclarationOverrideValidator(
        mutableListOf<RollingDeclarationOverrideRule>(
            RollingDeclarationOverrideCodePrefixRule("OPS"),
            RollingDeclarationOverrideCodeLengthRule(80),
            RollingDeclarationOverrideAllowedTitleRule(mutableListOf<String>("manual", "capped", "default")),
            RollingDeclarationOverrideRequiredTitleRule("rolling declaration override needs this field"),
        ),
    )
}
