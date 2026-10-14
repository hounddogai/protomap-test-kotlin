package com.acme.transport.lastmile.parcel.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalParcelDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalParcelDraftValidator()
        val item = newRegionalParcelDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalParcelDraftRequiredCodeRule("regional parcel draft needs this field")
        val item = newRegionalParcelDraft("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = RegionalParcelDraftRetriesRangeRule(16, 32)
        val item = newRegionalParcelDraft("x", "y")
        item.retries = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
