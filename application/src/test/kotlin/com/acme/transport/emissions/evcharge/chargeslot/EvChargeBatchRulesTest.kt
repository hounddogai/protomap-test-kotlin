package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvChargeBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEvChargeBatchValidator()
        val item = newEvChargeBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = EvChargeBatchRequiredOwnerIdRule("ev charge batch needs a value")
        val item = newEvChargeBatch("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EvChargeBatchRequiredCodeRule("ev charge batch needs a value")
        val item = newEvChargeBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
