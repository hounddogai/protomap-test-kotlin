package com.acme.transport.lastmile.drop.batcheddropnotice

/** A rule a weighted drop notice must pass. Check returns its problems, or none. */
interface WeightedDropNoticeRule {
    fun check(item: WeightedDropNotice): MutableList<String>
}

data class WeightedDropNoticeVehicleIdLengthRule(var maxLength: Int) : WeightedDropNoticeRule {
    override fun check(item: WeightedDropNotice): MutableList<String> {
        val size = item.vehicleId.length
        if (size > maxLength) {
            return mutableListOf<String>("vehicle id is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class WeightedDropNoticeRequiredVehicleIdRule(var message: String) : WeightedDropNoticeRule {
    override fun check(item: WeightedDropNotice): MutableList<String> {
        if (item.vehicleId.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDropNoticeRequiredCodeRule(var message: String) : WeightedDropNoticeRule {
    override fun check(item: WeightedDropNotice): MutableList<String> {
        if (item.code.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class WeightedDropNoticeContactEmailLengthRule(var maxLength: Int) : WeightedDropNoticeRule {
    override fun check(item: WeightedDropNotice): MutableList<String> {
        val size = item.contactEmail.length
        if (size > maxLength) {
            return mutableListOf<String>("contact email is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a weighted drop notice. */
data class WeightedDropNoticeValidator(var rules: MutableList<WeightedDropNoticeRule>) {
    fun validate(item: WeightedDropNotice): MutableList<String> {
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

    fun requireValid(item: WeightedDropNotice) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<WeightedDropNotice>): MutableMap<String, MutableList<String>> {
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

fun defaultWeightedDropNoticeValidator(): WeightedDropNoticeValidator {
    return WeightedDropNoticeValidator(
        mutableListOf<WeightedDropNoticeRule>(
            WeightedDropNoticeVehicleIdLengthRule(32),
            WeightedDropNoticeRequiredVehicleIdRule("weighted drop notice needs this field"),
            WeightedDropNoticeRequiredCodeRule("weighted drop notice needs an entry"),
            WeightedDropNoticeContactEmailLengthRule(64),
        ),
    )
}
