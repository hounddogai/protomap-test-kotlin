package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.kit.text.normalize

/** A rule a backhaul mapping must pass. Check returns its problems, or none. */
interface BackhaulMappingRule {
    fun check(item: BackhaulMapping): MutableList<String>
}

data class BackhaulMappingRequiredCodeRule(var message: String) : BackhaulMappingRule {
    override fun check(item: BackhaulMapping): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulMappingNotesLengthRule(var maxLength: Int) : BackhaulMappingRule {
    override fun check(item: BackhaulMapping): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulMappingLabelLengthRule(var maxLength: Int) : BackhaulMappingRule {
    override fun check(item: BackhaulMapping): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulMappingAllowedTrailerIdRule(var allowed: MutableList<String>) : BackhaulMappingRule {
    override fun check(item: BackhaulMapping): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.trailerId))) {
            return mutableListOf<String>("trailer id ${item.trailerId} is unknown")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul mapping. */
data class BackhaulMappingValidator(var rules: MutableList<BackhaulMappingRule>) {
    fun validate(item: BackhaulMapping): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulMapping) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulMapping>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulMappingValidator(): BackhaulMappingValidator {
    return BackhaulMappingValidator(
        mutableListOf<BackhaulMappingRule>(
            BackhaulMappingRequiredCodeRule("backhaul mapping needs this field"),
            BackhaulMappingNotesLengthRule(86400),
            BackhaulMappingLabelLengthRule(3600),
            BackhaulMappingAllowedTrailerIdRule(mutableListOf<String>()),
        ),
    )
}
