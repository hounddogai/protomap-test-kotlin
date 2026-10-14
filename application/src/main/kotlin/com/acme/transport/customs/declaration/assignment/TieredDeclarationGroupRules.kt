package com.acme.transport.customs.declaration.assignment

/** A rule a tiered declaration group must pass. Check returns its problems, or none. */
interface TieredDeclarationGroupRule {
    fun check(item: TieredDeclarationGroup): MutableList<String>
}

data class TieredDeclarationGroupNameLengthRule(var maxLength: Int) : TieredDeclarationGroupRule {
    override fun check(item: TieredDeclarationGroup): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredDeclarationGroupTagsLimitRule(
    var maxCount: Int,
    var banned: MutableList<String>,
) : TieredDeclarationGroupRule {
    override fun check(item: TieredDeclarationGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        if (item.tags.size > maxCount) {
            problems.add("has ${item.tags.size} tags, at most $maxCount allowed")
        }
        for (tag in item.tags) {
            if (banned.contains(tag)) {
                problems.add("tag $tag is not allowed")
            }
        }
        return problems
    }
}

data class TieredDeclarationGroupRequiredContactEmailRule(var message: String) : TieredDeclarationGroupRule {
    override fun check(item: TieredDeclarationGroup): MutableList<String> {
        if (item.contactEmail.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered declaration group. */
data class TieredDeclarationGroupValidator(var rules: MutableList<TieredDeclarationGroupRule>) {
    fun validate(item: TieredDeclarationGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredDeclarationGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredDeclarationGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredDeclarationGroupValidator(): TieredDeclarationGroupValidator {
    return TieredDeclarationGroupValidator(
        mutableListOf<TieredDeclarationGroupRule>(
            TieredDeclarationGroupNameLengthRule(32),
            TieredDeclarationGroupTagsLimitRule(255, mutableListOf<String>("adaptive", "batched")),
            TieredDeclarationGroupRequiredContactEmailRule("tiered declaration group needs an entry"),
        ),
    )
}
