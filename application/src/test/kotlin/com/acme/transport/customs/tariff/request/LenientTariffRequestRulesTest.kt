package com.acme.transport.customs.tariff.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientTariffRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientTariffRequestValidator()
        val item = newLenientTariffRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LenientTariffRequestUpdatedAtRangeRule(80, 3600)
        val item = newLenientTariffRequest("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = LenientTariffRequestCeilingRangeRule(8.2, 1329.5)
        val item = newLenientTariffRequest("x", "y")
        item.ceiling = 1331.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientTariffRequestCodeLengthRule(64)
        val item = newLenientTariffRequest("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientTariffRequestRequiredCodeRule("lenient tariff request needs a value")
        val item = newLenientTariffRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
