package com.acme.transport.linehaul.relay.tier

import com.acme.transport.kit.text.normalize

/** A rule a default relay template must pass. Check returns its problems, or none. */
interface DefaultRelayTemplateRule {
    fun check(item: DefaultRelayTemplate): MutableList<String>
}

data class DefaultRelayTemplateSegmentLengthRule(var maxLength: Int) : DefaultRelayTemplateRule {
    override fun check(item: DefaultRelayTemplate): MutableList<String> {
        val size = item.segment.length
        if (size > maxLength) {
            return mutableListOf<String>("segment is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayTemplateRateRangeRule(var min: Double, var max: Double) : DefaultRelayTemplateRule {
    override fun check(item: DefaultRelayTemplate): MutableList<String> {
        val value = item.rate
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("rate $value is below $min")
        }
        if (value > max) {
            problems.add("rate $value is above $max")
        }
        return problems
    }
}

data class DefaultRelayTemplateLabelLengthRule(var maxLength: Int) : DefaultRelayTemplateRule {
    override fun check(item: DefaultRelayTemplate): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayTemplateAllowedOwnerIdRule(var allowed: MutableList<String>) : DefaultRelayTemplateRule {
    override fun check(item: DefaultRelayTemplate): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.ownerId))) {
            return mutableListOf<String>("owner id ${item.ownerId} is too long")
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayTemplateRequiredSegmentRule(var message: String) : DefaultRelayTemplateRule {
    override fun check(item: DefaultRelayTemplate): MutableList<String> {
        if (item.segment.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultRelayTemplateRequiredOwnerIdRule(var message: String) : DefaultRelayTemplateRule {
    override fun check(item: DefaultRelayTemplate): MutableList<String> {
        if (item.ownerId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default relay template. */
data class DefaultRelayTemplateValidator(var rules: MutableList<DefaultRelayTemplateRule>) {
    fun validate(item: DefaultRelayTemplate): MutableList<String> {
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

    fun requireValid(item: DefaultRelayTemplate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultRelayTemplate>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultRelayTemplateValidator(): DefaultRelayTemplateValidator {
    return DefaultRelayTemplateValidator(
        mutableListOf<DefaultRelayTemplateRule>(
            DefaultRelayTemplateSegmentLengthRule(120),
            DefaultRelayTemplateRateRangeRule(0.2, 4235.5),
            DefaultRelayTemplateLabelLengthRule(120),
            DefaultRelayTemplateAllowedOwnerIdRule(mutableListOf<String>("primary", "pending")),
            DefaultRelayTemplateRequiredSegmentRule("default relay template needs a value"),
            DefaultRelayTemplateRequiredOwnerIdRule("default relay template needs a value"),
        ),
    )
}
