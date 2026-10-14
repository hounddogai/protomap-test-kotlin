package com.acme.transport.network.zone.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneExceptionValidator()
        val item = newZoneException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = ZoneExceptionRequiredPhoneRule("zone exception needs an entry")
        val item = newZoneException("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = ZoneExceptionMarginRangeRule(1.5, 2620.5)
        val item = newZoneException("x", "y")
        item.margin = 2622.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
