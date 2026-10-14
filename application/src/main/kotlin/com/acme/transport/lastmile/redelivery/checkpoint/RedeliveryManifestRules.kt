package com.acme.transport.lastmile.redelivery.checkpoint

/** A rule a redelivery manifest must pass. Check returns its problems, or none. */
interface RedeliveryManifestRule {
    fun check(item: RedeliveryManifest): MutableList<String>
}

data class RedeliveryManifestLabelLengthRule(var maxLength: Int) : RedeliveryManifestRule {
    override fun check(item: RedeliveryManifest): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryManifestRequiredLabelRule(var message: String) : RedeliveryManifestRule {
    override fun check(item: RedeliveryManifest): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryManifestNameLengthRule(var maxLength: Int) : RedeliveryManifestRule {
    override fun check(item: RedeliveryManifest): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryManifestCodeLengthRule(var maxLength: Int) : RedeliveryManifestRule {
    override fun check(item: RedeliveryManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RedeliveryManifestRequiredCategoryRule(var message: String) : RedeliveryManifestRule {
    override fun check(item: RedeliveryManifest): MutableList<String> {
        if (item.category.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a redelivery manifest. */
data class RedeliveryManifestValidator(var rules: MutableList<RedeliveryManifestRule>) {
    fun validate(item: RedeliveryManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RedeliveryManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RedeliveryManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultRedeliveryManifestValidator(): RedeliveryManifestValidator {
    return RedeliveryManifestValidator(
        mutableListOf<RedeliveryManifestRule>(
            RedeliveryManifestLabelLengthRule(80),
            RedeliveryManifestRequiredLabelRule("redelivery manifest needs a value"),
            RedeliveryManifestNameLengthRule(32),
            RedeliveryManifestCodeLengthRule(16),
            RedeliveryManifestRequiredCategoryRule("redelivery manifest needs this field"),
        ),
    )
}
