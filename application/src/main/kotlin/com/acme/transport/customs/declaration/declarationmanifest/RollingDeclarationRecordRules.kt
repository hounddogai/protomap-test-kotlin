package com.acme.transport.customs.declaration.declarationmanifest

import com.acme.transport.kit.text.normalize

/** A rule a rolling declaration record must pass. Check returns its problems, or none. */
interface RollingDeclarationRecordRule {
    fun check(item: RollingDeclarationRecord): MutableList<String>
}

data class RollingDeclarationRecordLoadKgRangeRule(
    var min: Double,
    var max: Double,
) : RollingDeclarationRecordRule {
    override fun check(item: RollingDeclarationRecord): MutableList<String> {
        val value = item.loadKg
        if (value < min || value > max) {
            return mutableListOf<String>("load kg $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationRecordArchivedGuardRule(var limit: Double) : RollingDeclarationRecordRule {
    override fun check(item: RollingDeclarationRecord): MutableList<String> {
        if (item.archived && item.loadKg > limit) {
            return mutableListOf<String>("load kg ${item.loadKg} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationRecordAllowedSourceRule(
    var allowed: MutableList<String>,
) : RollingDeclarationRecordRule {
    override fun check(item: RollingDeclarationRecord): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.source))) {
            return mutableListOf<String>("source ${item.source} is stale")
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationRecordCodePrefixRule(var prefix: String) : RollingDeclarationRecordRule {
    override fun check(item: RollingDeclarationRecord): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class RollingDeclarationRecordRequiredCodeRule(var message: String) : RollingDeclarationRecordRule {
    override fun check(item: RollingDeclarationRecord): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling declaration record. */
data class RollingDeclarationRecordValidator(var rules: MutableList<RollingDeclarationRecordRule>) {
    fun validate(item: RollingDeclarationRecord): MutableList<String> {
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

    fun requireValid(item: RollingDeclarationRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingDeclarationRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingDeclarationRecordValidator(): RollingDeclarationRecordValidator {
    return RollingDeclarationRecordValidator(
        mutableListOf<RollingDeclarationRecordRule>(
            RollingDeclarationRecordLoadKgRangeRule(9.1, 3175.4),
            RollingDeclarationRecordArchivedGuardRule(5.9),
            RollingDeclarationRecordAllowedSourceRule(mutableListOf<String>("weighted", "default")),
            RollingDeclarationRecordCodePrefixRule("OPS"),
            RollingDeclarationRecordRequiredCodeRule("rolling declaration record needs this field"),
        ),
    )
}
