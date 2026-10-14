package com.acme.transport.crewing.qualification.target

/** A rule a qualification reservation must pass. Check returns its problems, or none. */
interface QualificationReservationRule {
    fun check(item: QualificationReservation): MutableList<String>
}

data class QualificationReservationBatchSizeRangeRule(
    var min: Int,
    var max: Int,
) : QualificationReservationRule {
    override fun check(item: QualificationReservation): MutableList<String> {
        val value = item.batchSize
        if (value < min || value > max) {
            return mutableListOf<String>("batch size $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationReservationCodePrefixRule(var prefix: String) : QualificationReservationRule {
    override fun check(item: QualificationReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class QualificationReservationEndsAtRangeRule(var min: Int, var max: Int) : QualificationReservationRule {
    override fun check(item: QualificationReservation): MutableList<String> {
        val value = item.endsAt
        if (value < min || value > max) {
            return mutableListOf<String>("ends at $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationReservationCostRangeRule(
    var min: Double,
    var max: Double,
) : QualificationReservationRule {
    override fun check(item: QualificationReservation): MutableList<String> {
        val value = item.cost
        if (value < min || value > max) {
            return mutableListOf<String>("cost $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class QualificationReservationCodeLengthRule(var maxLength: Int) : QualificationReservationRule {
    override fun check(item: QualificationReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a qualification reservation. */
data class QualificationReservationValidator(var rules: MutableList<QualificationReservationRule>) {
    fun validate(item: QualificationReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: QualificationReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<QualificationReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultQualificationReservationValidator(): QualificationReservationValidator {
    return QualificationReservationValidator(
        mutableListOf<QualificationReservationRule>(
            QualificationReservationBatchSizeRangeRule(64, 1000),
            QualificationReservationCodePrefixRule("OPS"),
            QualificationReservationEndsAtRangeRule(32, 80),
            QualificationReservationCostRangeRule(3.7, 2393.9),
            QualificationReservationCodeLengthRule(16),
        ),
    )
}
