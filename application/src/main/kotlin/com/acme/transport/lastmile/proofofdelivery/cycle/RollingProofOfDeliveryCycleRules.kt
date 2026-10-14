package com.acme.transport.lastmile.proofofdelivery.cycle

/** A rule a rolling proof of delivery cycle must pass. Check returns its problems, or none. */
interface RollingProofOfDeliveryCycleRule {
    fun check(item: RollingProofOfDeliveryCycle): MutableList<String>
}

data class RollingProofOfDeliveryCycleRequiredCodeRule(var message: String) : RollingProofOfDeliveryCycleRule {
    override fun check(item: RollingProofOfDeliveryCycle): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryCycleExternalRefLengthRule(
    var maxLength: Int,
) : RollingProofOfDeliveryCycleRule {
    override fun check(item: RollingProofOfDeliveryCycle): MutableList<String> {
        val size = item.externalRef.length
        if (size > maxLength) {
            return mutableListOf<String>("external ref is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryCycleRequiredDepotRule(var message: String) : RollingProofOfDeliveryCycleRule {
    override fun check(item: RollingProofOfDeliveryCycle): MutableList<String> {
        if (item.depot.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class RollingProofOfDeliveryCycleCodeLengthRule(var maxLength: Int) : RollingProofOfDeliveryCycleRule {
    override fun check(item: RollingProofOfDeliveryCycle): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a rolling proof of delivery cycle. */
data class RollingProofOfDeliveryCycleValidator(var rules: MutableList<RollingProofOfDeliveryCycleRule>) {
    fun validate(item: RollingProofOfDeliveryCycle): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: RollingProofOfDeliveryCycle) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<RollingProofOfDeliveryCycle>): MutableMap<String, MutableList<String>> {
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

fun defaultRollingProofOfDeliveryCycleValidator(): RollingProofOfDeliveryCycleValidator {
    return RollingProofOfDeliveryCycleValidator(
        mutableListOf<RollingProofOfDeliveryCycleRule>(
            RollingProofOfDeliveryCycleRequiredCodeRule("rolling proof of delivery cycle needs an entry"),
            RollingProofOfDeliveryCycleExternalRefLengthRule(1000),
            RollingProofOfDeliveryCycleRequiredDepotRule("rolling proof of delivery cycle needs this field"),
            RollingProofOfDeliveryCycleCodeLengthRule(64),
        ),
    )
}
