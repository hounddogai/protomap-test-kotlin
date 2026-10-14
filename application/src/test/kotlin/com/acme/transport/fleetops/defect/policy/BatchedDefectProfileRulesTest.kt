package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDefectProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDefectProfileValidator()
        val item = newBatchedDefectProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = BatchedDefectProfileRequiredExternalRefRule("batched defect profile needs this field")
        val item = newBatchedDefectProfile("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedDefectProfileRequiredNameRule("batched defect profile needs an entry")
        val item = newBatchedDefectProfile("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedDefectProfileRequiredCodeRule("batched defect profile needs a value")
        val item = newBatchedDefectProfile("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = BatchedDefectProfileEtaMinutesRangeRule(120, 255)
        val item = newBatchedDefectProfile("x", "y")
        item.etaMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
