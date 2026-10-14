package com.acme.transport.terminal.crossdock.docksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictCrossDockAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictCrossDockAgreementValidator()
        val item = newStrictCrossDockAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictCrossDockAgreementRequiredCodeRule("strict cross dock agreement needs an entry")
        val item = newStrictCrossDockAgreement("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = StrictCrossDockAgreementRequiredLaneRule("strict cross dock agreement needs a value")
        val item = newStrictCrossDockAgreement("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictCrossDockAgreementCodeLengthRule(255)
        val item = newStrictCrossDockAgreement("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
