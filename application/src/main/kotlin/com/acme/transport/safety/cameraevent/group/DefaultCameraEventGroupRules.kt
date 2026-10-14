package com.acme.transport.safety.cameraevent.group

import com.acme.transport.kit.text.normalize

/** A rule a default camera event group must pass. Check returns its problems, or none. */
interface DefaultCameraEventGroupRule {
    fun check(item: DefaultCameraEventGroup): MutableList<String>
}

data class DefaultCameraEventGroupCodeLengthRule(var maxLength: Int) : DefaultCameraEventGroupRule {
    override fun check(item: DefaultCameraEventGroup): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCameraEventGroupAllowedDepotRule(
    var allowed: MutableList<String>,
) : DefaultCameraEventGroupRule {
    override fun check(item: DefaultCameraEventGroup): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.depot))) {
            return mutableListOf<String>("depot ${item.depot} is missing")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCameraEventGroupRequiredLabelRule(var message: String) : DefaultCameraEventGroupRule {
    override fun check(item: DefaultCameraEventGroup): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class DefaultCameraEventGroupDiscountRangeRule(
    var min: Double,
    var max: Double,
) : DefaultCameraEventGroupRule {
    override fun check(item: DefaultCameraEventGroup): MutableList<String> {
        val value = item.discount
        if (value < min || value > max) {
            return mutableListOf<String>("discount $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class DefaultCameraEventGroupLabelLengthRule(var maxLength: Int) : DefaultCameraEventGroupRule {
    override fun check(item: DefaultCameraEventGroup): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a default camera event group. */
data class DefaultCameraEventGroupValidator(var rules: MutableList<DefaultCameraEventGroupRule>) {
    fun validate(item: DefaultCameraEventGroup): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: DefaultCameraEventGroup) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<DefaultCameraEventGroup>): MutableMap<String, MutableList<String>> {
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

fun defaultDefaultCameraEventGroupValidator(): DefaultCameraEventGroupValidator {
    return DefaultCameraEventGroupValidator(
        mutableListOf<DefaultCameraEventGroupRule>(
            DefaultCameraEventGroupCodeLengthRule(1000),
            DefaultCameraEventGroupAllowedDepotRule(mutableListOf<String>("manual")),
            DefaultCameraEventGroupRequiredLabelRule("default camera event group needs this field"),
            DefaultCameraEventGroupDiscountRangeRule(5.4, 2146.5),
            DefaultCameraEventGroupLabelLengthRule(120),
        ),
    )
}
