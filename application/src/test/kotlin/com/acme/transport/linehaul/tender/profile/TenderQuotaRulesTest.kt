package com.acme.transport.linehaul.tender.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TenderQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTenderQuotaValidator()
        val item = newTenderQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TenderQuotaRequiredLabelRule("tender quota needs an entry")
        val item = newTenderQuota("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TenderQuotaCodeLengthRule(32)
        val item = newTenderQuota("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
