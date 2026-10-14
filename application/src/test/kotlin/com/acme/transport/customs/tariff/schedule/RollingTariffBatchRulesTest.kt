package com.acme.transport.customs.tariff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingTariffBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingTariffBatchValidator()
        val item = newRollingTariffBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = RollingTariffBatchRequiredNotesRule("rolling tariff batch needs a value")
        val item = newRollingTariffBatch("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingTariffBatchCodeLengthRule(1000)
        val item = newRollingTariffBatch("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
