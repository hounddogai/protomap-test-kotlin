package com.acme.transport.linehaul.trailer.trailerrule

import com.acme.transport.kit.text.normalize

/** A rule a batched trailer entry must pass. Check returns its problems, or none. */
interface BatchedTrailerEntryRule {
    fun check(item: BatchedTrailerEntry): MutableList<String>
}

data class BatchedTrailerEntryLabelLengthRule(var maxLength: Int) : BatchedTrailerEntryRule {
    override fun check(item: BatchedTrailerEntry): MutableList<String> {
        val size = item.label.length
        if (size > maxLength) {
            return mutableListOf<String>("label is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerEntryAllowedCodeRule(var allowed: MutableList<String>) : BatchedTrailerEntryRule {
    override fun check(item: BatchedTrailerEntry): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is below minimum")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerEntryRequiredChecksumRule(var message: String) : BatchedTrailerEntryRule {
    override fun check(item: BatchedTrailerEntry): MutableList<String> {
        if (item.checksum.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerEntryRequiredLabelRule(var message: String) : BatchedTrailerEntryRule {
    override fun check(item: BatchedTrailerEntry): MutableList<String> {
        if (item.label.trim() == "") {
            return mutableListOf<String>(message)
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerEntryDistanceKmRangeRule(var min: Double, var max: Double) : BatchedTrailerEntryRule {
    override fun check(item: BatchedTrailerEntry): MutableList<String> {
        val value = item.distanceKm
        if (value < min || value > max) {
            return mutableListOf<String>("distance km $value is outside $min to $max")
        }
        return mutableListOf<String>()
    }
}

data class BatchedTrailerEntryChecksumLengthRule(var maxLength: Int) : BatchedTrailerEntryRule {
    override fun check(item: BatchedTrailerEntry): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a batched trailer entry. */
data class BatchedTrailerEntryValidator(var rules: MutableList<BatchedTrailerEntryRule>) {
    fun validate(item: BatchedTrailerEntry): MutableList<String> {
        val problems = mutableListOf<String>()
        for (rule in rules) {
            for (problem in rule.check(item)) {
                problems.add(problem)
            }
        }
        return problems
    }

    fun requireValid(item: BatchedTrailerEntry) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.displayLabel()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<BatchedTrailerEntry>): MutableMap<String, MutableList<String>> {
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

fun defaultBatchedTrailerEntryValidator(): BatchedTrailerEntryValidator {
    return BatchedTrailerEntryValidator(
        mutableListOf<BatchedTrailerEntryRule>(
            BatchedTrailerEntryLabelLengthRule(80),
            BatchedTrailerEntryAllowedCodeRule(mutableListOf<String>("tiered")),
            BatchedTrailerEntryRequiredChecksumRule("batched trailer entry needs a value"),
            BatchedTrailerEntryRequiredLabelRule("batched trailer entry needs a value"),
            BatchedTrailerEntryDistanceKmRangeRule(6.2, 2020.2),
            BatchedTrailerEntryChecksumLengthRule(120),
        ),
    )
}
