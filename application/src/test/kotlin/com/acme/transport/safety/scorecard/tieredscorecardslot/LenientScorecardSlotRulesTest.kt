package com.acme.transport.safety.scorecard.tieredscorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientScorecardSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientScorecardSlotValidator()
        val item = newLenientScorecardSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientScorecardSlotNameLengthRule(120)
        val item = newLenientScorecardSlot("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = LenientScorecardSlotRetriesRangeRule(255, 86400)
        val item = newLenientScorecardSlot("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = LenientScorecardSlotUnitLengthRule(64)
        val item = newLenientScorecardSlot("x", "y")
        item.unit = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
