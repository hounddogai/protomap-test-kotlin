package com.acme.transport.customs.declaration.budget

import com.acme.transport.kit.text.normalize

/** A rule a rolling declaration tier must pass. Check returns its problems, or none. */
interface RollingDeclarationTierRule {
    fun check(item: RollingDeclarationTier): MutableList<String>
}

data class RollingDeclarationTierDescriptionLengthRule(var maxLength: Int) : RollingDeclarationTierRule {
    override fun check(item: RollingDeclarationTier): MutableList<String> {
        val size = item.description.length
        if (size > maxLength) {
            return mutableListOf<String>("description is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationTierSourceLengthRule(var maxLength: Int) : RollingDeclarationTierRule {
    override fun check(item: RollingDeclarationTier): MutableList<String> {
        val size = item.source.length
        if (size > maxLength) {
            return mutableListOf<String>("source is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationTierRequiredCodeRule(var message: String) : RollingDeclarationTierRule {
    override fun check(item: RollingDeclarationTier): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationTierAllowedNameRule(
    var allowed: MutableList<String>,
) : RollingDeclarationTierRule {
    override fun check(item: RollingDeclarationTier): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.name))) {
            return mutableListOf<String>("name ${item.name} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationTierCodeLengthRule(var maxLength: Int) : RollingDeclarationTierRule {
    override fun check(item: RollingDeclarationTier): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling declaration tier. */
data class RollingDeclarationTierValidator(var rules: MutableList<RollingDeclarationTierRule>) {
    fun validate(item: RollingDeclarationTier): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingDeclarationTier) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDeclarationTier>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDeclarationTierValidator(): RollingDeclarationTierValidator {
    return RollingDeclarationTierValidator(
        mutableListOf<RollingDeclarationTierRule>(
            RollingDeclarationTierDescriptionLengthRule(1000),
            RollingDeclarationTierSourceLengthRule(32),
            RollingDeclarationTierRequiredCodeRule("rolling declaration tier needs this field"),
            RollingDeclarationTierAllowedNameRule(mutableListOf<String>("weighted", "tiered", "primary")),
            RollingDeclarationTierCodeLengthRule(1000),
        ),
    )
}
