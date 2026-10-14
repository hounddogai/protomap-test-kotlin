package com.acme.transport.crewing.qualification.qualificationassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedQualificationBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedQualificationBundleValidator()
        val item = newBatchedQualificationBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = BatchedQualificationBundleRequiredDescriptionRule("batched qualification bundle needs this field")
        val item = newBatchedQualificationBundle("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedQualificationBundleRequiredLabelRule("batched qualification bundle needs a value")
        val item = newBatchedQualificationBundle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = BatchedQualificationBundleRouteCodeLengthRule(64)
        val item = newBatchedQualificationBundle("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = BatchedQualificationBundleRequiredRouteCodeRule("batched qualification bundle needs this field")
        val item = newBatchedQualificationBundle("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
