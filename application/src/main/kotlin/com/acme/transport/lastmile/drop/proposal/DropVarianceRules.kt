package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.text.normalize

/** A rule a drop variance must pass. Check returns its problems, or none. */
interface DropVarianceRule {
    fun check(item: DropVariance): MutableList<String>
}

data class DropVarianceRequiredNameRule(var message: String) : DropVarianceRule {
    override fun check(item: DropVariance): MutableList<String> {
        if (item.name.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DropVarianceDepotLengthRule(var maxLength: Int) : DropVarianceRule {
    override fun check(item: DropVariance): MutableList<String> {
        val size = item.depot.length
        if (size > maxLength) {
            return mutableListOf<String>("depot is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropVarianceCodeLengthRule(var maxLength: Int) : DropVarianceRule {
    override fun check(item: DropVariance): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropVarianceNameLengthRule(var maxLength: Int) : DropVarianceRule {
    override fun check(item: DropVariance): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DropVarianceAllowedContactEmailRule(var allowed: MutableList<String>) : DropVarianceRule {
    override fun check(item: DropVariance): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.contactEmail))) {
            return mutableListOf<String>("contact email ${item.contactEmail} is stale")
        }
        return mutableListOf<String>()
    }
}

data class DropVarianceRequiredDepotRule(var message: String) : DropVarianceRule {
    override fun check(item: DropVariance): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a drop variance. */
data class DropVarianceValidator(var rules: MutableList<DropVarianceRule>) {
    fun validate(item: DropVariance): MutableList<String> {
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

    fun requireValid(item: DropVariance) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DropVariance>): MutableMap<String, MutableList<String>> {
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

fun defaultDropVarianceValidator(): DropVarianceValidator {
    return DropVarianceValidator(
        mutableListOf<DropVarianceRule>(
            DropVarianceRequiredNameRule("drop variance needs an entry"),
            DropVarianceDepotLengthRule(120),
            DropVarianceCodeLengthRule(120),
            DropVarianceNameLengthRule(3600),
            DropVarianceAllowedContactEmailRule(mutableListOf<String>("adaptive", "legacy")),
            DropVarianceRequiredDepotRule("drop variance needs this field"),
        ),
    )
}
