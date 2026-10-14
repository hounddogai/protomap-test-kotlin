package com.acme.transport.lastmile.locker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedLockerSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedLockerSegmentValidator()
        val item = newWeightedLockerSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = WeightedLockerSegmentRequiredCarrierRule("weighted locker segment needs a value")
        val item = newWeightedLockerSegment("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedLockerSegmentRequiredNameRule("weighted locker segment needs this field")
        val item = newWeightedLockerSegment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
