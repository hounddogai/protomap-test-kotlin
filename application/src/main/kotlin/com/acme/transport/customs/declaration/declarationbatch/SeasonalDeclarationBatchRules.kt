package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.kit.text.normalize

/** A rule a seasonal declaration batch must pass. Check returns its problems, or none. */
interface SeasonalDeclarationBatchRule {
    fun check(item: SeasonalDeclarationBatch): MutableList<String>
}

data class SeasonalDeclarationBatchCodeLengthRule(var maxLength: Int) : SeasonalDeclarationBatchRule {
    override fun check(item: SeasonalDeclarationBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationBatchTitleLengthRule(var maxLength: Int) : SeasonalDeclarationBatchRule {
    override fun check(item: SeasonalDeclarationBatch): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationBatchVisibleGuardRule(var limit: Double) : SeasonalDeclarationBatchRule {
    override fun check(item: SeasonalDeclarationBatch): MutableList<String> {
        if (item.visible && item.maximum > limit) {
            return mutableListOf<String>("maximum ${item.maximum} exceeds $limit while visible")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationBatchAllowedOwnerIdRule(
    var allowed: MutableList<String>,
) : SeasonalDeclarationBatchRule {
    override fun check(item: SeasonalDeclarationBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is not allowed")
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationBatchRequiredCodeRule(var message: String) : SeasonalDeclarationBatchRule {
    override fun check(item: SeasonalDeclarationBatch): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class SeasonalDeclarationBatchRequiredOwnerIdRule(var message: String) : SeasonalDeclarationBatchRule {
    override fun check(item: SeasonalDeclarationBatch): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a seasonal declaration batch. */
data class SeasonalDeclarationBatchValidator(var rules: MutableList<SeasonalDeclarationBatchRule>) {
    fun validate(item: SeasonalDeclarationBatch): MutableList<String> {
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

    fun requireValid(item: SeasonalDeclarationBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<SeasonalDeclarationBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultSeasonalDeclarationBatchValidator(): SeasonalDeclarationBatchValidator {
    return SeasonalDeclarationBatchValidator(
        mutableListOf<SeasonalDeclarationBatchRule>(
            SeasonalDeclarationBatchCodeLengthRule(16),
            SeasonalDeclarationBatchTitleLengthRule(32),
            SeasonalDeclarationBatchVisibleGuardRule(7.1),
            SeasonalDeclarationBatchAllowedOwnerIdRule(mutableListOf<String>("scheduled")),
            SeasonalDeclarationBatchRequiredCodeRule("seasonal declaration batch needs this field"),
            SeasonalDeclarationBatchRequiredOwnerIdRule("seasonal declaration batch needs this field"),
        ),
    )
}
