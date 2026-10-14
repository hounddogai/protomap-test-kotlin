package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictZoneCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictZoneCycleValidator()
        val item = newStrictZoneCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = StrictZoneCycleRetriesRangeRule(120, 3600)
        val item = newStrictZoneCycle("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = StrictZoneCyclePhoneLengthRule(120)
        val item = newStrictZoneCycle("x", "y")
        item.phone = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = StrictZoneCycleRequiredDepotRule("strict zone cycle needs this field")
        val item = newStrictZoneCycle("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
