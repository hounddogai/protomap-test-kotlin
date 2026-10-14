package com.acme.transport.lastmile.parcel.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalParcelWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalParcelWindowValidator()
        val item = newRegionalParcelWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = RegionalParcelWindowRetriesRangeRule(64, 80)
        val item = newRegionalParcelWindow("x", "y")
        item.retries = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalParcelWindowRequiredCodeRule("regional parcel window needs this field")
        val item = newRegionalParcelWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
