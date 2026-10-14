package com.acme.transport.linehaul.tender.group

import com.acme.transport.kit.text.normalize

/** A rule a fallback tender batch must pass. Check returns its problems, or none. */
interface FallbackTenderBatchRule {
    fun check(item: FallbackTenderBatch): MutableList<String>
}

data class FallbackTenderBatchCodeLengthRule(var maxLength: Int) : FallbackTenderBatchRule {
    override fun check(item: FallbackTenderBatch): MutableList<String> {
        val size = item.code.length
        if (size > maxLength) {
            return mutableListOf<String>("code is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTenderBatchNameLengthRule(var maxLength: Int) : FallbackTenderBatchRule {
    override fun check(item: FallbackTenderBatch): MutableList<String> {
        val size = item.name.length
        if (size > maxLength) {
            return mutableListOf<String>("name is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTenderBatchChecksumLengthRule(var maxLength: Int) : FallbackTenderBatchRule {
    override fun check(item: FallbackTenderBatch): MutableList<String> {
        val size = item.checksum.length
        if (size > maxLength) {
            return mutableListOf<String>("checksum is $size characters, more than $maxLength")
        }
        return mutableListOf<String>()
    }
}

data class FallbackTenderBatchAllowedCodeRule(var allowed: MutableList<String>) : FallbackTenderBatchRule {
    override fun check(item: FallbackTenderBatch): MutableList<String> {
        if (allowed.size == 0) {
            return mutableListOf<String>()
        }
        if (!allowed.contains(normalize(item.code))) {
            return mutableListOf<String>("code ${item.code} is stale")
        }
        return mutableListOf<String>()
    }
}

/** Runs every rule of a fallback tender batch. */
data class FallbackTenderBatchValidator(var rules: MutableList<FallbackTenderBatchRule>) {
    fun validate(item: FallbackTenderBatch): MutableList<String> {
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

    fun requireValid(item: FallbackTenderBatch) {
        val problems = validate(item)
        if (problems.size > 0) {
            error("${item.label()} is invalid: ${problems.joinToString("; ")}")
        }
    }

    fun validateAll(items: MutableList<FallbackTenderBatch>): MutableMap<String, MutableList<String>> {
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

fun defaultFallbackTenderBatchValidator(): FallbackTenderBatchValidator {
    return FallbackTenderBatchValidator(
        mutableListOf<FallbackTenderBatchRule>(
            FallbackTenderBatchCodeLengthRule(16),
            FallbackTenderBatchNameLengthRule(64),
            FallbackTenderBatchChecksumLengthRule(3600),
            FallbackTenderBatchAllowedCodeRule(mutableListOf<String>("tiered")),
        ),
    )
}
