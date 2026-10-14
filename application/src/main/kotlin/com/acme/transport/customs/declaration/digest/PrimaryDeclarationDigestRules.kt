package com.acme.transport.customs.declaration.digest

import com.acme.transport.kit.text.normalize

/** A rule a primary declaration digest must pass. Check returns its problems, or none. */
interface PrimaryDeclarationDigestRule {
    fun check(item: PrimaryDeclarationDigest): MutableList<String>
}

data class PrimaryDeclarationDigestRequiredCodeRule(var message: String) : PrimaryDeclarationDigestRule {
    override fun check(item: PrimaryDeclarationDigest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDeclarationDigestCodeLengthRule(var maxLength: Int) : PrimaryDeclarationDigestRule {
    override fun check(item: PrimaryDeclarationDigest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDeclarationDigestLabelLengthRule(var maxLength: Int) : PrimaryDeclarationDigestRule {
    override fun check(item: PrimaryDeclarationDigest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDeclarationDigestMinimumRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryDeclarationDigestRule {
    override fun check(item: PrimaryDeclarationDigest): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryDeclarationDigestAllowedTitleRule(
    var allowed: MutableList<String>,
) : PrimaryDeclarationDigestRule {
    override fun check(item: PrimaryDeclarationDigest): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.title))) {
            return mutableListOf<String>("title ${item.title} is below minimum")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a primary declaration digest. */
data class PrimaryDeclarationDigestValidator(var rules: MutableList<PrimaryDeclarationDigestRule>) {
    fun validate(item: PrimaryDeclarationDigest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryDeclarationDigest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryDeclarationDigest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryDeclarationDigestValidator(): PrimaryDeclarationDigestValidator {
    return PrimaryDeclarationDigestValidator(
        mutableListOf<PrimaryDeclarationDigestRule>(
            PrimaryDeclarationDigestRequiredCodeRule("primary declaration digest needs a value"),
            PrimaryDeclarationDigestCodeLengthRule(120),
            PrimaryDeclarationDigestLabelLengthRule(80),
            PrimaryDeclarationDigestMinimumRangeRule(8.4, 4224.4),
            PrimaryDeclarationDigestAllowedTitleRule(mutableListOf<String>("adaptive", "manual", "legacy")),
        ),
    )
}
