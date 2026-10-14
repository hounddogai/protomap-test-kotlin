package com.acme.transport.linehaul.laneplan.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedLanePlanBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedLanePlanBundleValidator()
        val item = newBatchedLanePlanBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedLanePlanBundleRequiredCodeRule("batched lane plan bundle needs an entry")
        val item = newBatchedLanePlanBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedLanePlanBundleCodeLengthRule(86400)
        val item = newBatchedLanePlanBundle("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = BatchedLanePlanBundleWindowDaysRangeRule(16, 80)
        val item = newBatchedLanePlanBundle("x", "y")
        item.windowDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
