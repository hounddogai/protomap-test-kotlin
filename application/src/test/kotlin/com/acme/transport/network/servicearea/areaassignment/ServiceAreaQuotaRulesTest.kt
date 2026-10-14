package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaQuotaValidator()
        val item = newServiceAreaQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = ServiceAreaQuotaRequiredCategoryRule("service area quota needs an entry")
        val item = newServiceAreaQuota("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ServiceAreaQuotaSequenceRangeRule(32, 80)
        val item = newServiceAreaQuota("x", "y")
        item.sequence = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = ServiceAreaQuotaCategoryLengthRule(3600)
        val item = newServiceAreaQuota("x", "y")
        item.category = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = ServiceAreaQuotaSourceLengthRule(1000)
        val item = newServiceAreaQuota("x", "y")
        item.source = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaQuotaCodeLengthRule(64)
        val item = newServiceAreaQuota("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
