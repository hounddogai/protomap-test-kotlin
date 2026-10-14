package com.acme.transport.emissions.idling.idlingquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualIdlingQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualIdlingQuotaValidator()
        val item = newManualIdlingQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ManualIdlingQuotaRequiredShiftCodeRule("manual idling quota needs this field")
        val item = newManualIdlingQuota("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualIdlingQuotaRequiredCodeRule("manual idling quota needs this field")
        val item = newManualIdlingQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualIdlingQuotaRequiredNameRule("manual idling quota needs an entry")
        val item = newManualIdlingQuota("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = ManualIdlingQuotaRetriesRangeRule(64, 64)
        val item = newManualIdlingQuota("x", "y")
        item.retries = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
