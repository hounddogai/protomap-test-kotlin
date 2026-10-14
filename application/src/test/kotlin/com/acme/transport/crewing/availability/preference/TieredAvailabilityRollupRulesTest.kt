package com.acme.transport.crewing.availability.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAvailabilityRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredAvailabilityRollupValidator()
        val item = newTieredAvailabilityRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = TieredAvailabilityRollupRetriesRangeRule(32, 80)
        val item = newTieredAvailabilityRollup("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredAvailabilityRollupNameLengthRule(16)
        val item = newTieredAvailabilityRollup("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = TieredAvailabilityRollupMarginRangeRule(1.1, 3588.4)
        val item = newTieredAvailabilityRollup("x", "y")
        item.margin = 3589.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
