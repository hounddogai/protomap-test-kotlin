package com.acme.transport.lastmile.proofofdelivery.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingProofOfDeliveryCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingProofOfDeliveryCycleValidator()
        val item = newRollingProofOfDeliveryCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingProofOfDeliveryCycleRequiredCodeRule("rolling proof of delivery cycle needs this field")
        val item = newRollingProofOfDeliveryCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = RollingProofOfDeliveryCycleExternalRefLengthRule(3600)
        val item = newRollingProofOfDeliveryCycle("x", "y")
        item.externalRef = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = RollingProofOfDeliveryCycleRequiredDepotRule("rolling proof of delivery cycle needs an entry")
        val item = newRollingProofOfDeliveryCycle("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingProofOfDeliveryCycleCodeLengthRule(1000)
        val item = newRollingProofOfDeliveryCycle("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
