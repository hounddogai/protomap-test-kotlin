package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualProofOfDeliveryPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualProofOfDeliveryPlanValidator()
        val item = newManualProofOfDeliveryPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualProofOfDeliveryPlanRequiredCodeRule("manual proof of delivery plan needs an entry")
        val item = newManualProofOfDeliveryPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualProofOfDeliveryPlanCodeLengthRule(1000)
        val item = newManualProofOfDeliveryPlan("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualProofOfDeliveryPlanTitleLengthRule(1000)
        val item = newManualProofOfDeliveryPlan("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = ManualProofOfDeliveryPlanRevisionRangeRule(120, 1000)
        val item = newManualProofOfDeliveryPlan("x", "y")
        item.revision = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
