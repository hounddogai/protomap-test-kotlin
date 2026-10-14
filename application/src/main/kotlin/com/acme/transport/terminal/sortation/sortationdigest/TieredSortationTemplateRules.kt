package com.acme.transport.terminal.sortation.sortationdigest

/** A rule a tiered sortation template must pass. Check returns its problems, or none. */
interface TieredSortationTemplateRule {
    fun check(item: TieredSortationTemplate): MutableList<String>
}

data class TieredSortationTemplateCodePrefixRule(var prefix: String) : TieredSortationTemplateRule {
    override fun check(item: TieredSortationTemplate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationTemplateContactEmailLengthRule(var maxLength: Int) : TieredSortationTemplateRule {
    override fun check(item: TieredSortationTemplate): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationTemplateNameLengthRule(var maxLength: Int) : TieredSortationTemplateRule {
    override fun check(item: TieredSortationTemplate): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationTemplateRequiredNameRule(var message: String) : TieredSortationTemplateRule {
    override fun check(item: TieredSortationTemplate): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class TieredSortationTemplateSegmentLengthRule(var maxLength: Int) : TieredSortationTemplateRule {
    override fun check(item: TieredSortationTemplate): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered sortation template. */
data class TieredSortationTemplateValidator(var rules: MutableList<TieredSortationTemplateRule>) {
    fun validate(item: TieredSortationTemplate): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredSortationTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredSortationTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredSortationTemplateValidator(): TieredSortationTemplateValidator {
    return TieredSortationTemplateValidator(
        mutableListOf<TieredSortationTemplateRule>(
            TieredSortationTemplateCodePrefixRule("REF"),
            TieredSortationTemplateContactEmailLengthRule(86400),
            TieredSortationTemplateNameLengthRule(3600),
            TieredSortationTemplateRequiredNameRule("tiered sortation template needs this field"),
            TieredSortationTemplateSegmentLengthRule(32),
        ),
    )
}
