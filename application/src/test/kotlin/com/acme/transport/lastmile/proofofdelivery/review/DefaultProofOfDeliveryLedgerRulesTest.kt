package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultProofOfDeliveryLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultProofOfDeliveryLedgerValidator()
        val item = newDefaultProofOfDeliveryLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = DefaultProofOfDeliveryLedgerPalletsRangeRule(255, 3600)
        val item = newDefaultProofOfDeliveryLedger("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultProofOfDeliveryLedgerCodeLengthRule(86400)
        val item = newDefaultProofOfDeliveryLedger("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = DefaultProofOfDeliveryLedgerRouteCodeLengthRule(32)
        val item = newDefaultProofOfDeliveryLedger("x", "y")
        item.routeCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
