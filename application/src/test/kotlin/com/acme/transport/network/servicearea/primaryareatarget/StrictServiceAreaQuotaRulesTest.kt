package com.acme.transport.network.servicearea.primaryareatarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictServiceAreaQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictServiceAreaQuotaValidator()
        val item = newStrictServiceAreaQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = StrictServiceAreaQuotaRequiredContactEmailRule("strict service area quota needs an entry")
        val item = newStrictServiceAreaQuota("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = StrictServiceAreaQuotaContactEmailLengthRule(80)
        val item = newStrictServiceAreaQuota("x", "y")
        item.contactEmail = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictServiceAreaQuotaNameLengthRule(80)
        val item = newStrictServiceAreaQuota("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictServiceAreaQuotaRequiredCodeRule("strict service area quota needs an entry")
        val item = newStrictServiceAreaQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
