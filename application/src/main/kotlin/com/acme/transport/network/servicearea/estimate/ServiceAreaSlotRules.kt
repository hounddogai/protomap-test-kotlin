package com.acme.transport.network.servicearea.estimate

/** A rule a service area slot must pass. Check returns its problems, or none. */
interface ServiceAreaSlotRule {
    fun check(item: ServiceAreaSlot): MutableList<String>
}

data class ServiceAreaSlotCodePrefixRule(var prefix: String) : ServiceAreaSlotRule {
    override fun check(item: ServiceAreaSlot): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaSlotCodeLengthRule(var maxLength: Int) : ServiceAreaSlotRule {
    override fun check(item: ServiceAreaSlot): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaSlotLabelLengthRule(var maxLength: Int) : ServiceAreaSlotRule {
    override fun check(item: ServiceAreaSlot): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class ServiceAreaSlotMaximumRangeRule(var min: Double, var max: Double) : ServiceAreaSlotRule {
    override fun check(item: ServiceAreaSlot): MutableList<String> {
        val value = item.maximum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("maximum $value is below $min")
        }
        if (value > max) {
            problems.add("maximum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a service area slot. */
data class ServiceAreaSlotValidator(var rules: MutableList<ServiceAreaSlotRule>) {
    fun validate(item: ServiceAreaSlot): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: ServiceAreaSlot) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<ServiceAreaSlot>): MutableMap<String, MutableList<String>> {
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

fun defaultServiceAreaSlotValidator(): ServiceAreaSlotValidator {
    return ServiceAreaSlotValidator(
        mutableListOf<ServiceAreaSlotRule>(
            ServiceAreaSlotCodePrefixRule("OPS"),
            ServiceAreaSlotCodeLengthRule(16),
            ServiceAreaSlotLabelLengthRule(255),
            ServiceAreaSlotMaximumRangeRule(3.5, 2109.2),
        ),
    )
}
