package com.acme.transport.customs.declaration.declarationmanifest

/** A rule a legacy declaration manifest must pass. Check returns its problems, or none. */
interface LegacyDeclarationManifestRule {
    fun check(item: LegacyDeclarationManifest): MutableList<String>
}

data class LegacyDeclarationManifestRequiredChannelRule(var message: String) : LegacyDeclarationManifestRule {
    override fun check(item: LegacyDeclarationManifest): MutableList<String> {
        if (item.channel.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationManifestChannelLengthRule(var maxLength: Int) : LegacyDeclarationManifestRule {
    override fun check(item: LegacyDeclarationManifest): MutableList<String> {
        val size = item.channel.length
        if (size > maxLength) {
            return mutableListOf<String>("channel is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationManifestUpdatedAtRangeRule(
    var min: Int,
    var max: Int,
) : LegacyDeclarationManifestRule {
    override fun check(item: LegacyDeclarationManifest): MutableList<String> {
        val value = item.updatedAt
        if (value < min || value > max) {
            return mutableListOf<String>("updated at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationManifestRequiredLabelRule(var message: String) : LegacyDeclarationManifestRule {
    override fun check(item: LegacyDeclarationManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationManifestRequiredCodeRule(var message: String) : LegacyDeclarationManifestRule {
    override fun check(item: LegacyDeclarationManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class LegacyDeclarationManifestCeilingRangeRule(
    var min: Double,
    var max: Double,
) : LegacyDeclarationManifestRule {
    override fun check(item: LegacyDeclarationManifest): MutableList<String> {
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

/** Runs every rule of a legacy declaration manifest. */
data class LegacyDeclarationManifestValidator(var rules: MutableList<LegacyDeclarationManifestRule>) {
    fun validate(item: LegacyDeclarationManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: LegacyDeclarationManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<LegacyDeclarationManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultLegacyDeclarationManifestValidator(): LegacyDeclarationManifestValidator {
    return LegacyDeclarationManifestValidator(
        mutableListOf<LegacyDeclarationManifestRule>(
            LegacyDeclarationManifestRequiredChannelRule("legacy declaration manifest needs a value"),
            LegacyDeclarationManifestChannelLengthRule(32),
            LegacyDeclarationManifestUpdatedAtRangeRule(255, 86400),
            LegacyDeclarationManifestRequiredLabelRule("legacy declaration manifest needs this field"),
            LegacyDeclarationManifestRequiredCodeRule("legacy declaration manifest needs this field"),
            LegacyDeclarationManifestCeilingRangeRule(9.6, 4952.6),
        ),
    )
}
