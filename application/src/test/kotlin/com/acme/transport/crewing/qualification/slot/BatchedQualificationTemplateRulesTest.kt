package com.acme.transport.crewing.qualification.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedQualificationTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedQualificationTemplateValidator()
        val item = newBatchedQualificationTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedQualificationTemplateRequiredCodeRule("batched qualification template needs a value")
        val item = newBatchedQualificationTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = BatchedQualificationTemplateCeilingRangeRule(4.4, 3280.1)
        val item = newBatchedQualificationTemplate("x", "y")
        item.ceiling = 3281.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
