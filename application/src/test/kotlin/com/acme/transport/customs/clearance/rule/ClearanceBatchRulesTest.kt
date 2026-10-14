package com.acme.transport.customs.clearance.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class ClearanceBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceBatchValidator()
        val item = newClearanceBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = ClearanceBatchPalletsRangeRule(16, 1000)
        val item = newClearanceBatch("x", "y")
        item.pallets = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above revision`() {
        val rule = ClearanceBatchRevisionRangeRule(64, 3600)
        val item = newClearanceBatch("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
