package com.acme.transport.safety.cameraevent.reservation

/** A rule a camera event reservation must pass. Check returns its problems, or none. */
interface CameraEventReservationRule {
    fun check(item: CameraEventReservation): MutableList<String>
}

data class CameraEventReservationCodePrefixRule(var prefix: String) : CameraEventReservationRule {
    override fun check(item: CameraEventReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventReservationRatioRangeRule(var min: Double, var max: Double) : CameraEventReservationRule {
    override fun check(item: CameraEventReservation): MutableList<String> {
        val value = item.ratio
        if (value < min || value > max) {
            return mutableListOf<String>("ratio $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class CameraEventReservationTitleLengthRule(var maxLength: Int) : CameraEventReservationRule {
    override fun check(item: CameraEventReservation): MutableList<String> {
        val size = item.title.length
        if (size > maxLength) {
            return mutableListOf<String>("title is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a camera event reservation. */
data class CameraEventReservationValidator(var rules: MutableList<CameraEventReservationRule>) {
    fun validate(item: CameraEventReservation): MutableList<String> {
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

    fun requireValid(item: CameraEventReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<CameraEventReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultCameraEventReservationValidator(): CameraEventReservationValidator {
    return CameraEventReservationValidator(
        mutableListOf<CameraEventReservationRule>(
            CameraEventReservationCodePrefixRule("OPS"),
            CameraEventReservationRatioRangeRule(0.6, 2408.1),
            CameraEventReservationTitleLengthRule(3600),
        ),
    )
}
