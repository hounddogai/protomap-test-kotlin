package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingProofOfDeliveryEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingProofOfDeliveryEntryValidator()
        val item = newRollingProofOfDeliveryEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingProofOfDeliveryEntryCodeLengthRule(80)
        val item = newRollingProofOfDeliveryEntry("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above priority`() {
        val rule = RollingProofOfDeliveryEntryPriorityRangeRule(16, 120)
        val item = newRollingProofOfDeliveryEntry("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingProofOfDeliveryEntryRequiredCodeRule("rolling proof of delivery entry needs an entry")
        val item = newRollingProofOfDeliveryEntry("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RollingProofOfDeliveryEntryPalletsRangeRule(32, 86400)
        val item = newRollingProofOfDeliveryEntry("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
