package com.acme.transport.safety.cameraevent.recorditem

/** A rule a fallback camera event record must pass. Check returns its problems, or none. */
interface FallbackCameraEventRecordRule {
    fun check(item: FallbackCameraEventRecord): MutableList<String>
}

data class FallbackCameraEventRecordEtaMinutesRangeRule(
    var min: Int,
    var max: Int,
) : FallbackCameraEventRecordRule {
    override fun check(item: FallbackCameraEventRecord): MutableList<String> {
        val value = item.etaMinutes
        if (value < min || value > max) {
            return mutableListOf<String>("eta minutes $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class FallbackCameraEventRecordRequiredDockRule(var message: String) : FallbackCameraEventRecordRule {
    override fun check(item: FallbackCameraEventRecord): MutableList<String> {
        if (item.dock.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class FallbackCameraEventRecordQuantityRangeRule(
    var min: Int,
    var max: Int,
) : FallbackCameraEventRecordRule {
    override fun check(item: FallbackCameraEventRecord): MutableList<String> {
        val value = item.quantity
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("quantity $value is below $min")
        }
        if (value > max) {
            problems.add("quantity $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a fallback camera event record. */
data class FallbackCameraEventRecordValidator(var rules: MutableList<FallbackCameraEventRecordRule>) {
    fun validate(item: FallbackCameraEventRecord): MutableList<String> {
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

    fun requireValid(item: FallbackCameraEventRecord) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackCameraEventRecord>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackCameraEventRecordValidator(): FallbackCameraEventRecordValidator {
    return FallbackCameraEventRecordValidator(
        mutableListOf<FallbackCameraEventRecordRule>(
            FallbackCameraEventRecordEtaMinutesRangeRule(32, 3600),
            FallbackCameraEventRecordRequiredDockRule("fallback camera event record needs an entry"),
            FallbackCameraEventRecordQuantityRangeRule(120, 86400),
        ),
    )
}
