package com.acme.transport.safety.cameraevent.cycle

/** A rule a camera event forecast must pass. Check returns its problems, or none. */
interface CameraEventForecastRule {
    fun check(item: CameraEventForecast): MutableList<String>
}

data class CameraEventForecastRequiredSummaryRule(var message: String) : CameraEventForecastRule {
    override fun check(item: CameraEventForecast): MutableList<String> {
        if (item.summary.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventForecastRequiredLabelRule(var message: String) : CameraEventForecastRule {
    override fun check(item: CameraEventForecast): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class CameraEventForecastChecksumLengthRule(var maxLength: Int) : CameraEventForecastRule {
    override fun check(item: CameraEventForecast): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventForecastMinimumRangeRule(var min: Double, var max: Double) : CameraEventForecastRule {
    override fun check(item: CameraEventForecast): MutableList<String> {
        val value = item.minimum
        val problems = mutableListOf<String>()
        if (value < min) {
            problems.add("minimum $value is below $min")
        }
        if (value > max) {
            problems.add("minimum $value is above $max")
        }
        return problems
    }
}

/** Runs every rule of a camera event forecast. */
data class CameraEventForecastValidator(var rules: MutableList<CameraEventForecastRule>) {
    fun validate(item: CameraEventForecast): MutableList<String> {
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

    fun requireValid(item: CameraEventForecast) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventForecast>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventForecastValidator(): CameraEventForecastValidator {
    return CameraEventForecastValidator(
        mutableListOf<CameraEventForecastRule>(
            CameraEventForecastRequiredSummaryRule("camera event forecast needs an entry"),
            CameraEventForecastRequiredLabelRule("camera event forecast needs an entry"),
            CameraEventForecastChecksumLengthRule(32),
            CameraEventForecastMinimumRangeRule(4.1, 1068.7),
        ),
    )
}
