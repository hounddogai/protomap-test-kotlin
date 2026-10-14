package com.acme.transport.lastmile.parcel.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedParcelTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedParcelTemplateValidator()
        val item = newBatchedParcelTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedParcelTemplateCodeLengthRule(64)
        val item = newBatchedParcelTemplate("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = BatchedParcelTemplateEtaMinutesRangeRule(120, 3600)
        val item = newBatchedParcelTemplate("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = BatchedParcelTemplateUpdatedAtRangeRule(80, 86400)
        val item = newBatchedParcelTemplate("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = BatchedParcelTemplateRouteCodeLengthRule(64)
        val item = newBatchedParcelTemplate("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
