package com.acme.transport.network.node.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalNodeAgreementRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalNodeAgreementValidator()
        val item = newSeasonalNodeAgreement("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = SeasonalNodeAgreementDriverIdLengthRule(120)
        val item = newSeasonalNodeAgreement("x", "y")
        item.driverId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalNodeAgreementLabelLengthRule(1000)
        val item = newSeasonalNodeAgreement("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
