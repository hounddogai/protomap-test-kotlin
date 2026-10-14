package com.acme.transport.customs.clearance.clearancemanifest

/** A rule a primary clearance manifest must pass. Check returns its problems, or none. */
interface PrimaryClearanceManifestRule {
    fun check(item: PrimaryClearanceManifest): MutableList<String>
}

data class PrimaryClearanceManifestRequiredCodeRule(var message: String) : PrimaryClearanceManifestRule {
    override fun check(item: PrimaryClearanceManifest): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceManifestMinimumRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryClearanceManifestRule {
    override fun check(item: PrimaryClearanceManifest): MutableList<String> {
        val value = item.minimum
        if (value < min || value > max) {
            return mutableListOf<String>("minimum $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceManifestCodeLengthRule(var maxLength: Int) : PrimaryClearanceManifestRule {
    override fun check(item: PrimaryClearanceManifest): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceManifestCarrierLengthRule(var maxLength: Int) : PrimaryClearanceManifestRule {
    override fun check(item: PrimaryClearanceManifest): MutableList<String> {
        val size = item.carrier.length
        if (size > maxLength) {
            return mutableListOf<String>("carrier is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class PrimaryClearanceManifestCapacityRangeRule(
    var min: Int,
    var max: Int,
) : PrimaryClearanceManifestRule {
    override fun check(item: PrimaryClearanceManifest): MutableList<String> {
        val value = item.capacity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("capacity $value is below $min")
        }
        if (value > max) {
            problems.add("capacity $value is above $max")
        }
        return problems
    }
}

data class PrimaryClearanceManifestMaximumRangeRule(
    var min: Double,
    var max: Double,
) : PrimaryClearanceManifestRule {
    override fun check(item: PrimaryClearanceManifest): MutableList<String> {
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

/** Runs every rule of a primary clearance manifest. */
data class PrimaryClearanceManifestValidator(var rules: MutableList<PrimaryClearanceManifestRule>) {
    fun validate(item: PrimaryClearanceManifest): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: PrimaryClearanceManifest) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<PrimaryClearanceManifest>): MutableMap<String, MutableList<String>> {
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

fun defaultPrimaryClearanceManifestValidator(): PrimaryClearanceManifestValidator {
    return PrimaryClearanceManifestValidator(
        mutableListOf<PrimaryClearanceManifestRule>(
            PrimaryClearanceManifestRequiredCodeRule("primary clearance manifest needs an entry"),
            PrimaryClearanceManifestMinimumRangeRule(5.0, 794.0),
            PrimaryClearanceManifestCodeLengthRule(255),
            PrimaryClearanceManifestCarrierLengthRule(64),
            PrimaryClearanceManifestCapacityRangeRule(16, 120),
            PrimaryClearanceManifestMaximumRangeRule(8.7, 1238.0),
        ),
    )
}
