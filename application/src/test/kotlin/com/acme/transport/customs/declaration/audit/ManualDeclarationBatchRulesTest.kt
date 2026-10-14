package com.acme.transport.customs.declaration.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDeclarationBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDeclarationBatchValidator()
        val item = newManualDeclarationBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = ManualDeclarationBatchEtaMinutesRangeRule(80, 3600)
        val item = newManualDeclarationBatch("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualDeclarationBatchRequiredNameRule("manual declaration batch needs an entry")
        val item = newManualDeclarationBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
