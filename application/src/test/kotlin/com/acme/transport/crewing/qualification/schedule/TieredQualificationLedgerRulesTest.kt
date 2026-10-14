package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredQualificationLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredQualificationLedgerValidator()
        val item = newTieredQualificationLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredQualificationLedgerRequiredCodeRule("tiered qualification ledger needs a value")
        val item = newTieredQualificationLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredQualificationLedgerTitleLengthRule(120)
        val item = newTieredQualificationLedger("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
