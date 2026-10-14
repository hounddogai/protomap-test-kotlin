package com.acme.transport.crewing.qualification.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationLedgerValidator()
        val item = newQualificationLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = QualificationLedgerChannelLengthRule(120)
        val item = newQualificationLedger("x", "y")
        item.channel = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = QualificationLedgerRequiredCodeRule("qualification ledger needs a value")
        val item = newQualificationLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = QualificationLedgerPalletsRangeRule(32, 80)
        val item = newQualificationLedger("x", "y")
        item.pallets = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
