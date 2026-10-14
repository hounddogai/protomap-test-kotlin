package com.acme.transport.network.cutoff.globalcutoffledger

/** A rule a tiered cutoff reservation must pass. Check returns its problems, or none. */
interface TieredCutoffReservationRule {
    fun check(item: TieredCutoffReservation): MutableList<String>
}

data class TieredCutoffReservationCodeLengthRule(var maxLength: Int) : TieredCutoffReservationRule {
    override fun check(item: TieredCutoffReservation): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class TieredCutoffReservationCodePrefixRule(var prefix: String) : TieredCutoffReservationRule {
    override fun check(item: TieredCutoffReservation): MutableList<String> {
        if (!item.code.uppercase().startsWith(prefix.uppercase())) {
            return mutableListOf<String>("code must start with $prefix")
        }
        return mutableListOf<String>()
    }
}

data class TieredCutoffReservationApprovedGuardRule(var limit: Int) : TieredCutoffReservationRule {
    override fun check(item: TieredCutoffReservation): MutableList<String> {
        if (item.approved && item.odometer > limit) {
            return mutableListOf<String>("odometer ${item.odometer} exceeds $limit while approved")
        }
        return mutableListOf<String>()
    }
}

data class TieredCutoffReservationShiftCodeLengthRule(var maxLength: Int) : TieredCutoffReservationRule {
    override fun check(item: TieredCutoffReservation): MutableList<String> {
        val size = item.shiftCode.length
        if (size > maxLength) {
            return mutableListOf<String>("shift code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a tiered cutoff reservation. */
data class TieredCutoffReservationValidator(var rules: MutableList<TieredCutoffReservationRule>) {
    fun validate(item: TieredCutoffReservation): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: TieredCutoffReservation) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<TieredCutoffReservation>): MutableMap<String, MutableList<String>> {
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

fun defaultTieredCutoffReservationValidator(): TieredCutoffReservationValidator {
    return TieredCutoffReservationValidator(
        mutableListOf<TieredCutoffReservationRule>(
            TieredCutoffReservationCodeLengthRule(86400),
            TieredCutoffReservationCodePrefixRule("OPS"),
            TieredCutoffReservationApprovedGuardRule(16),
            TieredCutoffReservationShiftCodeLengthRule(3600),
        ),
    )
}
