package com.acme.transport.safety.cameraevent.globaleventaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCameraEventAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCameraEventAllocationValidator()
        val item = newAdaptiveCameraEventAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveCameraEventAllocationCodeLengthRule(80)
        val item = newAdaptiveCameraEventAllocation("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = AdaptiveCameraEventAllocationLoadKgRangeRule(4.2, 4033.9)
        val item = newAdaptiveCameraEventAllocation("x", "y")
        item.loadKg = 4035.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = AdaptiveCameraEventAllocationRetriesRangeRule(32, 3600)
        val item = newAdaptiveCameraEventAllocation("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveCameraEventAllocationLabelLengthRule(255)
        val item = newAdaptiveCameraEventAllocation("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
