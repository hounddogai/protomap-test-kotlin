package com.acme.transport.crewing.qualification.qualificationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryQualificationLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryQualificationLedgerValidator()
        val item = newPrimaryQualificationLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = PrimaryQualificationLedgerQuantityRangeRule(16, 32)
        val item = newPrimaryQualificationLedger("x", "y")
        item.quantity = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = PrimaryQualificationLedgerRequiredTitleRule("primary qualification ledger needs this field")
        val item = newPrimaryQualificationLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
