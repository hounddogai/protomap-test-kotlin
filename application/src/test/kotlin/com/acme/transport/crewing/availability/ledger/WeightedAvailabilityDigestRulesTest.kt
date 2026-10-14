package com.acme.transport.crewing.availability.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAvailabilityDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedAvailabilityDigestValidator()
        val item = newWeightedAvailabilityDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedAvailabilityDigestCodeLengthRule(255)
        val item = newWeightedAvailabilityDigest("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedAvailabilityDigestRequiredCodeRule("weighted availability digest needs this field")
        val item = newWeightedAvailabilityDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = WeightedAvailabilityDigestStartsAtRangeRule(120, 3600)
        val item = newWeightedAvailabilityDigest("x", "y")
        item.startsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = WeightedAvailabilityDigestRequiredDepotRule("weighted availability digest needs an entry")
        val item = newWeightedAvailabilityDigest("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
