package com.acme.transport.linehaul.backhaul.threshold

/** A rule a backhaul window must pass. Check returns its problems, or none. */
interface BackhaulWindowRule {
    fun check(item: BackhaulWindow): MutableList<String>
}

data class BackhaulWindowRequiredCodeRule(var message: String) : BackhaulWindowRule {
    override fun check(item: BackhaulWindow): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BackhaulWindowPriorityRangeRule(var min: Int, var max: Int) : BackhaulWindowRule {
    override fun check(item: BackhaulWindow): MutableList<String> {
        val value = item.priority
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("priority $value is below $min")
        }
        if (value > max) {
            problems.add("priority $value is above $max")
        }
        return problems
    }
}

data class BackhaulWindowCodeLengthRule(var maxLength: Int) : BackhaulWindowRule {
    override fun check(item: BackhaulWindow): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulWindowVehicleIdLengthRule(var maxLength: Int) : BackhaulWindowRule {
    override fun check(item: BackhaulWindow): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BackhaulWindowCodePrefixRule(var prefix: String) : BackhaulWindowRule {
    override fun check(item: BackhaulWindow): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a backhaul window. */
data class BackhaulWindowValidator(var rules: MutableList<BackhaulWindowRule>) {
    fun validate(item: BackhaulWindow): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BackhaulWindow) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BackhaulWindow>): MutableMap<String, MutableList<String>> {
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

fun defaultBackhaulWindowValidator(): BackhaulWindowValidator {
    return BackhaulWindowValidator(
        mutableListOf<BackhaulWindowRule>(
            BackhaulWindowRequiredCodeRule("backhaul window needs a value"),
            BackhaulWindowPriorityRangeRule(16, 64),
            BackhaulWindowCodeLengthRule(86400),
            BackhaulWindowVehicleIdLengthRule(64),
            BackhaulWindowCodePrefixRule("OPS"),
        ),
    )
}
