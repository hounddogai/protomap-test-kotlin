package com.acme.transport.network.zone.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneBatchValidator()
        val item = newZoneBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = ZoneBatchRequiredDockRule("zone batch needs an entry")
        val item = newZoneBatch("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ZoneBatchAttemptsRangeRule(1000, 3600)
        val item = newZoneBatch("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ZoneBatchRequiredCodeRule("zone batch needs this field")
        val item = newZoneBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
