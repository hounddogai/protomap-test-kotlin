package com.acme.transport.linehaul.tender.profile

/** A rule a tender template must pass. Check returns its problems, or none. */
interface TenderTemplateRule {
    fun check(item: TenderTemplate): MutableList<String>
}

data class TenderTemplateRequiredLaneRule(var message: String) : TenderTemplateRule {
    override fun check(item: TenderTemplate): MutableList<String> {
        if (item.lane.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TenderTemplateLaneLengthRule(var maxLength: Int) : TenderTemplateRule {
    override fun check(item: TenderTemplate): MutableList<String> {
        val size = item.lane.length
        if (size > maxLength) {
            return mutableListOf<String>("lane is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TenderTemplateArchivedGuardRule(var limit: Int) : TenderTemplateRule {
    override fun check(item: TenderTemplate): MutableList<String> {
        if (item.archived && item.expiresAt > limit) {
            return mutableListOf<String>("expires at ${item.expiresAt} exceeds $limit while archived")
        }
        return mutableListOf<String>()
    }
}

data class TenderTemplateCodeLengthRule(var maxLength: Int) : TenderTemplateRule {
    override fun check(item: TenderTemplate): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tender template. */
data class TenderTemplateValidator(var rules: MutableList<TenderTemplateRule>) {
    fun validate(item: TenderTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TenderTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TenderTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultTenderTemplateValidator(): TenderTemplateValidator {
    return TenderTemplateValidator(
        mutableListOf<TenderTemplateRule>(
            TenderTemplateRequiredLaneRule("tender template needs an entry"),
            TenderTemplateLaneLengthRule(1000),
            TenderTemplateArchivedGuardRule(64),
            TenderTemplateCodeLengthRule(86400),
        ),
    )
}
