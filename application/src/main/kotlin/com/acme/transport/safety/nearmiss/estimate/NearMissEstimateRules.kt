package com.acme.transport.safety.nearmiss.estimate

/** A rule a near miss estimate must pass. Check returns its problems, or none. */
interface NearMissEstimateRule {
    fun check(item: NearMissEstimate): MutableList<String>
}

data class NearMissEstimateRequiredCodeRule(var message: String) : NearMissEstimateRule {
    override fun check(item: NearMissEstimate): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissEstimateCodePrefixRule(var prefix: String) : NearMissEstimateRule {
    override fun check(item: NearMissEstimate): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class NearMissEstimateRequiredLabelRule(var message: String) : NearMissEstimateRule {
    override fun check(item: NearMissEstimate): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissEstimateRequiredNotesRule(var message: String) : NearMissEstimateRule {
    override fun check(item: NearMissEstimate): MutableList<String> {
        if (item.notes.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class NearMissEstimateNotesLengthRule(var maxLength: Int) : NearMissEstimateRule {
    override fun check(item: NearMissEstimate): MutableList<String> {
        val size = item.notes.length
        if (size > maxLength) {
            return mutableListOf<String>("notes is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class NearMissEstimateVehicleIdLengthRule(var maxLength: Int) : NearMissEstimateRule {
    override fun check(item: NearMissEstimate): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a near miss estimate. */
data class NearMissEstimateValidator(var rules: MutableList<NearMissEstimateRule>) {
    fun validate(item: NearMissEstimate): MutableList<String> {
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

    fun requireValid(item: NearMissEstimate) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<NearMissEstimate>): MutableMap<String, MutableList<String>> {
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

fun defaultNearMissEstimateValidator(): NearMissEstimateValidator {
    return NearMissEstimateValidator(
        mutableListOf<NearMissEstimateRule>(
            NearMissEstimateRequiredCodeRule("near miss estimate needs this field"),
            NearMissEstimateCodePrefixRule("X"),
            NearMissEstimateRequiredLabelRule("near miss estimate needs a value"),
            NearMissEstimateRequiredNotesRule("near miss estimate needs this field"),
            NearMissEstimateNotesLengthRule(120),
            NearMissEstimateVehicleIdLengthRule(32),
        ),
    )
}
